package de.chennemann.plannr.server.banking

import de.chennemann.plannr.server.common.error.ApiException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import kotlinx.coroutines.reactor.awaitSingleOrNull
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Duration
import java.time.Instant
import java.util.Base64
import org.slf4j.LoggerFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface BankingProvider {
    suspend fun request(method: String, path: String, body: Any? = null, query: Map<String, String> = emptyMap()): JsonNode
}

@Component
class EnableBankingClient(
    private val mapper: ObjectMapper,
    @param:Value("\${plannr.banking.application-id:}") private val applicationId: String,
    @param:Value("\${plannr.banking.private-key-path:}") private val privateKeyPath: String,
    @Value("\${plannr.banking.base-url:https://api.enablebanking.com}") baseUrl: String,
) : BankingProvider {
    private val logger = LoggerFactory.getLogger(EnableBankingClient::class.java)
    private val client = WebClient.builder().baseUrl(baseUrl)
        .codecs { it.defaultCodecs().maxInMemorySize(8 * 1024 * 1024) }.build()
    private val key: PrivateKey by lazy {
        val pem = Files.readString(Path.of(privateKeyPath))
        require(pem.contains("-----BEGIN PRIVATE KEY-----")) { "Expected a PKCS#8 RSA private key" }
        val encoded = pem.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "").replace(Regex("\\s"), "")
        KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(encoded)))
    }

    override suspend fun request(method: String, path: String, body: Any?, query: Map<String, String>): JsonNode {
        if (applicationId.isBlank() || privateKeyPath.isBlank()) {
            throw ApiException(HttpStatus.SERVICE_UNAVAILABLE, "banking_not_configured", "Enable Banking is not configured")
        }
        var stage = "signing"
        try {
            val token = withContext(Dispatchers.IO) { jwt(mapper, applicationId, key, Instant.now()) }
            stage = "request"
            val request = client.method(org.springframework.http.HttpMethod.valueOf(method))
                .uri { builder ->
                    builder.path(path)
                    // URI variables receive strict encoding; literal '+' in a continuation token must not become a space.
                    val variables = mutableMapOf<String, String>()
                    query.entries.forEachIndexed { index, (name, value) ->
                        val variable = "query$index"
                        builder.queryParam(name, "{$variable}")
                        variables[variable] = value
                    }
                    builder.build(variables)
                }.headers { it.setBearerAuth(token) }
            val response = (if (body == null) request else request.bodyValue(body)).retrieve()
                .bodyToMono(String::class.java).timeout(Duration.ofSeconds(45))
            val responseBody = response.awaitSingleOrNull() ?: "{}"
            stage = "response-decoding"
            return mapper.readTree(responseBody)
        } catch (e: CancellationException) {
            throw e
        } catch (e: WebClientResponseException) {
            // Never expose upstream bodies, authorization codes, account data or tokens in errors/logs.
            val code = when (e.statusCode.value()) {
                429 -> "banking_rate_limited"
                401, 403 -> "banking_access_denied"
                404 -> "banking_resource_missing"
                else -> "banking_provider_error"
            }
            throw ApiException(if (e.statusCode.value() == 429) HttpStatus.TOO_MANY_REQUESTS else HttpStatus.BAD_GATEWAY,
                code, "Enable Banking request failed; retry or renew the connection")
        } catch (e: Exception) {
            // Class names and a fixed stage only: exception messages/stack traces may expose request URLs or bank data.
            logger.warn("Enable Banking {} failed at {}: exception={}, cause={}", method, stage,
                e.javaClass.simpleName, e.cause?.javaClass?.simpleName ?: "none")
            throw ApiException(HttpStatus.BAD_GATEWAY, "banking_provider_error", "Enable Banking is unavailable or its configuration is invalid")
        }
    }

    companion object {
        internal fun jwt(mapper: ObjectMapper, applicationId: String, key: PrivateKey, now: Instant): String {
            fun encode(bytes: ByteArray) = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
            val header = encode(mapper.writeValueAsBytes(mapOf("typ" to "JWT", "alg" to "RS256", "kid" to applicationId)))
            val payload = encode(mapper.writeValueAsBytes(mapOf("iss" to "enablebanking.com", "aud" to "api.enablebanking.com",
                "iat" to now.epochSecond, "exp" to now.plusSeconds(300).epochSecond)))
            val input = "$header.$payload"
            val signature = Signature.getInstance("SHA256withRSA").apply { initSign(key); update(input.toByteArray(Charsets.US_ASCII)) }.sign()
            return "$input.${encode(signature)}"
        }
    }
}
