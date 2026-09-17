package de.chennemann.plannr.server.partners.service

import de.chennemann.plannr.server.common.error.ValidationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.InetAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

internal const val MAX_LOGO_BYTES = 1_048_576

internal fun publicLogoAddress(address: InetAddress): Boolean {
    if (address.isAnyLocalAddress || address.isLoopbackAddress || address.isLinkLocalAddress ||
        address.isSiteLocalAddress || address.isMulticastAddress) return false
    val bytes = address.address.map { it.toInt() and 255 }
    return if (bytes.size == 4) {
        !(bytes[0] == 0 || bytes[0] >= 224 ||
            (bytes[0] == 100 && bytes[1] in 64..127) ||
            (bytes[0] == 169 && bytes[1] == 254) ||
            (bytes[0] == 192 && bytes[1] == 0) ||
            (bytes[0] == 198 && bytes[1] in 18..19))
    } else {
        // Only global-unicast IPv6; reject mapped, local, transition and reserved ranges.
        bytes[0] in 0x20..0x3f && !(bytes[0] == 0x20 && bytes[1] == 0x01 && bytes[2] == 0)
            && !(bytes[0] == 0x20 && bytes[1] == 0x02)
    }
}

@Component
class PartnerLogoLoader {
    private val client = OkHttpClient.Builder()
        .proxy(Proxy.NO_PROXY)
        .followRedirects(false).followSslRedirects(false)
        .connectTimeout(5, TimeUnit.SECONDS).readTimeout(5, TimeUnit.SECONDS)
        .callTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun preview(website: String): ByteArray = withContext(Dispatchers.IO) {
        if (website.length > 2048) invalidLogo("Website address is too long")
        val input = website.trim()
        val url = (if ("://" in input) input else "https://$input").toHttpUrlOrNull()
            ?: invalidLogo("Enter a valid website address")
        try {
            val (pageUrl, page) = download(url)
            // A direct image URL is also useful when a site's logo is not advertised in HTML.
            runCatching { normalizeLogo(page) }.getOrNull()?.let { return@withContext it }
            val document = Jsoup.parse(String(page, Charsets.UTF_8), pageUrl.toString())
            val candidates = document.select("link[href]").filter {
                it.attr("rel").lowercase().split(Regex("\\s+")).any { rel ->
                    rel == "icon" || rel == "apple-touch-icon" || rel == "apple-touch-icon-precomposed"
                }
            }.sortedByDescending { if ("apple-touch-icon" in it.attr("rel")) 1 else 0 }
                .mapNotNull { pageUrl.resolve(it.attr("href")) }
                .plus(listOfNotNull(pageUrl.resolve("/apple-touch-icon.png"), pageUrl.resolve("/favicon.ico")))
                .distinct().take(8)
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(25)
            for (candidate in candidates) {
                if (System.nanoTime() > deadline) break
                val logo = runCatching { normalizeLogo(download(candidate).second) }.getOrNull()
                if (logo != null) return@withContext logo
            }
            invalidLogo("No supported logo found. Try a direct PNG/JPEG image address or upload an image.")
        } catch (failure: ValidationException) {
            throw failure
        } catch (_: Exception) {
            invalidLogo("Could not load this website. Check the address or upload an image.")
        }
    }

    private fun download(initial: HttpUrl): Pair<HttpUrl, ByteArray> {
        var url = initial
        repeat(4) {
            if (url.port !in setOf(80, 443) || url.username.isNotEmpty() || url.password.isNotEmpty())
                invalidLogo("Only public HTTP or HTTPS websites are supported")
            val addresses = InetAddress.getAllByName(url.host).toList()
            if (addresses.isEmpty() || addresses.any { !publicLogoAddress(it) })
                invalidLogo("Only public websites are supported")
            // Pin validated addresses to this connection: no second DNS lookup/rebinding.
            val pinned = client.newBuilder().dns(object : okhttp3.Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    check(hostname == url.host)
                    return addresses
                }
            }).build()
            pinned.newCall(Request.Builder().url(url).header("User-Agent", "Plannr-PartnerLogo/1.0").build()).execute().use { response ->
                if (response.code in 300..399) {
                    url = url.resolve(response.header("Location") ?: invalidLogo("Invalid redirect"))
                        ?: invalidLogo("Invalid redirect")
                } else {
                    if (!response.isSuccessful) invalidLogo("Website did not return an image")
                    val body = response.body ?: invalidLogo("Empty response")
                    if (body.contentLength() > MAX_LOGO_BYTES) invalidLogo("Image or page is too large")
                    val bytes = body.byteStream().use { it.readNBytes(MAX_LOGO_BYTES + 1) }
                    if (bytes.size > MAX_LOGO_BYTES) invalidLogo("Image or page is too large")
                    return url to bytes
                }
            }
        }
        invalidLogo("Too many redirects")
    }
}

internal fun normalizeLogo(bytes: ByteArray): ByteArray = try {
    decodeLogo(bytes)
} catch (failure: ValidationException) {
    throw failure
} catch (_: Exception) {
    invalidLogo("The image is damaged or unsupported")
}

private fun decodeLogo(bytes: ByteArray): ByteArray {
    if (bytes.isEmpty() || bytes.size > MAX_LOGO_BYTES) invalidLogo("Choose an image smaller than 1 MB")
    ImageIO.createImageInputStream(ByteArrayInputStream(bytes)).use { input ->
        val readers = ImageIO.getImageReaders(input)
        if (!readers.hasNext()) invalidLogo("Use a PNG, JPEG, GIF or ICO image")
        val reader = readers.next()
        try {
            reader.input = input
            val width = reader.getWidth(0)
            val height = reader.getHeight(0)
            if (width !in 1..4096 || height !in 1..4096 || width.toLong() * height > 4_194_304)
                invalidLogo("Image dimensions are too large")
            val original = reader.read(0)
            val scale = minOf(1.0, 256.0 / maxOf(width, height))
            val result = BufferedImage(maxOf(1, (width * scale).toInt()), maxOf(1, (height * scale).toInt()), BufferedImage.TYPE_INT_ARGB)
            result.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
                drawImage(original, 0, 0, result.width, result.height, null)
                dispose()
            }
            return ByteArrayOutputStream().use { out ->
                ImageIO.write(result, "png", out)
                out.toByteArray()
            }
        } finally {
            reader.dispose()
        }
    }
}

internal fun invalidLogo(message: String): Nothing = throw ValidationException("invalid_logo", message)
