package de.chennemann.plannr.server.partners.service

import de.chennemann.plannr.server.common.error.ValidationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.InetAddress
import javax.imageio.ImageIO
import kotlin.test.*

class PartnerLogoLoaderTest {
    @Test
    fun `blocks private local mapped and metadata addresses`() {
        listOf("127.0.0.1", "0.0.0.0", "10.1.2.3", "172.16.0.1", "192.168.1.1",
            "169.254.169.254", "100.100.100.200", "224.0.0.1", "::1", "fc00::1", "fe80::1",
            "::ffff:127.0.0.1", "2002:7f00:1::").forEach {
            assertFalse(publicLogoAddress(InetAddress.getByName(it)), it)
        }
        assertTrue(publicLogoAddress(InetAddress.getByName("1.1.1.1")))
        assertTrue(publicLogoAddress(InetAddress.getByName("2606:4700:4700::1111")))
    }

    @Test
    fun `rejects nonpublic website before sending a request`() = runBlocking {
        for (url in listOf("http://127.0.0.1", "http://169.254.169.254", "http://localhost", "file:///etc/passwd", "http://example.com:22")) {
            assertFailsWith<ValidationException> { PartnerLogoLoader().preview(url) }
        }
    }

    @Test
    fun `normalizes to bounded PNG preserving aspect ratio and alpha`() {
        val image = BufferedImage(600, 300, BufferedImage.TYPE_INT_ARGB)
        image.setRGB(0, 0, 0x00ffffff)
        val bytes = ByteArrayOutputStream().apply { ImageIO.write(image, "png", this) }.toByteArray()
        val normalized = normalizeLogo(bytes)
        val decoded = ImageIO.read(ByteArrayInputStream(normalized))
        assertEquals(256, decoded.width)
        assertEquals(128, decoded.height)
        assertTrue(decoded.colorModel.hasAlpha())
        assertContentEquals(normalized, normalizeLogo(normalized))
    }

    @Test
    fun `rejects oversized invalid and vector payloads`() {
        listOf(ByteArray(MAX_LOGO_BYTES + 1), byteArrayOf(), byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10, 0), "<svg><script/></svg>".toByteArray()).forEach {
            assertFailsWith<ValidationException> { normalizeLogo(it) }
        }
        val tooWide = BufferedImage(4097, 1, BufferedImage.TYPE_INT_RGB)
        val bytes = ByteArrayOutputStream().apply { ImageIO.write(tooWide, "png", this) }.toByteArray()
        assertFailsWith<ValidationException> { normalizeLogo(bytes) }
    }
}
