package de.chennemann.plannr.server.banking

import com.sun.net.httpserver.HttpServer
import de.chennemann.plannr.server.common.error.ApiException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyPairGenerator
import java.util.Base64
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.*

class EnableBankingClientTest {
    @Test fun `authorization transport sends JSON request body`() = runBlocking<Unit> {
        val seen = AtomicReference<List<String>>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/auth") { exchange ->
            seen.set(listOf(exchange.requestMethod, exchange.requestHeaders.getFirst("Content-Type") ?: "",
                exchange.requestBody.readAllBytes().toString(Charsets.UTF_8)))
            val response = "{\"url\":\"https://example.com/authorize\"}".toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, response.size.toLong())
            exchange.responseBody.use { it.write(response) }
        }
        server.start()
        try {
            val client = EnableBankingClient(mapper, "app", key().toString(), "http://127.0.0.1:${server.address.port}")
            val body = mapOf("aspsp" to mapOf("name" to "C24", "country" to "DE"),
                "access" to mapOf("valid_until" to "2027-01-04T12:00:00Z"), "psu_type" to "personal")
            val result = client.request("POST", "/auth", body)
            assertEquals("https://example.com/authorize", result.get("url").asText())
            assertEquals("POST", seen.get()[0])
            assertTrue(seen.get()[1].startsWith("application/json"), seen.get()[1])
            assertEquals(mapper.valueToTree(body), mapper.readTree(seen.get()[2]))
        } finally { server.stop(0) }
    }

    @TempDir lateinit var directory: Path
    private val mapper = jacksonObjectMapper()
    private fun key(): Path {
        val pair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        return directory.resolve("key.pem").also {
            Files.writeString(it, "-----BEGIN PRIVATE KEY-----\n${Base64.getEncoder().encodeToString(pair.private.encoded)}\n-----END PRIVATE KEY-----")
        }
    }

    @Test fun `transport signs requests and percent encodes continuation tokens`() = runBlocking<Unit> {
        val seen = AtomicReference<List<String>>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            seen.set(listOf(exchange.requestMethod, exchange.requestURI.toASCIIString(), exchange.requestHeaders.getFirst("Authorization")))
            val body = "{\"transactions\":[]}".toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        try {
            val client = EnableBankingClient(mapper, "app", key().toString(), "http://127.0.0.1:${server.address.port}")
            val result = client.request("GET", "/accounts/uid/transactions", query=mapOf("continuation_key" to "a+b&c=x"))
            assertTrue(result.get("transactions").isArray)
            assertEquals("GET", seen.get()[0])
            assertTrue(seen.get()[1].contains("continuation_key=a%2Bb%26c%3Dx"), seen.get()[1])
            assertTrue(seen.get()[2].startsWith("Bearer ey"))
        } finally { server.stop(0) }
    }

    @Test fun `provider error bodies do not escape through API exceptions`() = runBlocking<Unit> {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val body = "sensitive-provider-account-data".toByteArray()
            exchange.sendResponseHeaders(429, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        try {
            val client = EnableBankingClient(mapper, "app", key().toString(), "http://127.0.0.1:${server.address.port}")
            val error = assertFailsWith<ApiException> { client.request("GET", "/aspsps") }
            assertEquals("banking_rate_limited", error.code)
            assertFalse(error.toString().contains("sensitive"))
            assertNull(error.cause)
        } finally { server.stop(0) }
    }

    @Test fun `unconfigured provider fails before network access`() = runBlocking<Unit> {
        val client = EnableBankingClient(mapper, "", "", "http://127.0.0.1:1")
        assertEquals("banking_not_configured", assertFailsWith<ApiException> { client.request("GET", "/aspsps") }.code)
    }
}
