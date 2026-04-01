package de.chennemann.plannr.server.currencies.api

import de.chennemann.plannr.server.currencies.support.CurrencyFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.support.expectApiError
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CurrencyApiIntegrationTest : ApiIntegrationTest() {
    @BeforeEach
    fun setUp() {
        cleanDatabase("currencies")
    }

    @Test
    fun `creates and fetches a currency over HTTP`() {
        createCurrencyOverHttp()

        webTestClient.get()
            .uri("/currencies/${CurrencyFixtures.DEFAULT_CODE}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.code").isEqualTo(CurrencyFixtures.DEFAULT_CODE)
            .jsonPath("$.symbol").isEqualTo(CurrencyFixtures.DEFAULT_SYMBOL)
    }

    @Test
    fun `lists empty array when no currencies exist`() {
        webTestClient.get()
            .uri("/currencies")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json("[]")
    }

    @Test
    fun `lists currencies ordered by code`() {
        createCurrencyOverHttp(CurrencyFixtures.createCurrencyRequest(code = "usd", name = "US Dollar", symbol = "\$"))
        createCurrencyOverHttp(CurrencyFixtures.createCurrencyRequest())

        webTestClient.get()
            .uri("/currencies")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].code").isEqualTo("EUR")
            .jsonPath("$[1].code").isEqualTo("USD")
    }

    @Test
    fun `fetches currency by lowercase code`() {
        createCurrencyOverHttp()

        webTestClient.get()
            .uri("/currencies/eur")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.code").isEqualTo("EUR")
            .jsonPath("$.name").isEqualTo(CurrencyFixtures.DEFAULT_NAME)
    }

    @Test
    fun `returns not found for unknown currency`() {
        webTestClient.get()
            .uri("/currencies/CHF")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .expectApiError(
                code = "not_found",
                message = "Currency not found",
                details = mapOf("code" to "CHF"),
            )
    }

    @Test
    fun `returns conflict for duplicate currency code`() {
        createCurrencyOverHttp()

        webTestClient.post()
            .uri("/currencies")
            .bodyValue(CurrencyFixtures.createCurrencyRequest())
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .expectApiError(
                code = "conflict",
                message = "Currency already exists",
                details = mapOf("code" to CurrencyFixtures.DEFAULT_CODE),
            )
    }

    @Test
    fun `returns conflict for duplicate currency code with different casing`() {
        createCurrencyOverHttp(CurrencyFixtures.createCurrencyRequest(code = "eur"))

        webTestClient.post()
            .uri("/currencies")
            .bodyValue(CurrencyFixtures.createCurrencyRequest(code = "EUR"))
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .expectApiError(
                code = "conflict",
                message = "Currency already exists",
                details = mapOf("code" to CurrencyFixtures.DEFAULT_CODE),
            )
    }

    @Test
    fun `updates an existing currency`() {
        createCurrencyOverHttp()

        webTestClient.put()
            .uri("/currencies/EUR")
            .bodyValue(
                CurrencyFixtures.updateCurrencyRequest(
                    code = "eur",
                    name = "Euro Updated",
                    symbol = "EUR",
                    decimalPlaces = 3,
                    symbolPosition = "after",
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.code").isEqualTo("EUR")
            .jsonPath("$.name").isEqualTo("Euro Updated")
            .jsonPath("$.symbol").isEqualTo("EUR")
            .jsonPath("$.decimalPlaces").isEqualTo(3)
            .jsonPath("$.symbolPosition").isEqualTo("after")
    }

    @Test
    fun `persists updated currency values`() {
        createCurrencyOverHttp()

        webTestClient.put()
            .uri("/currencies/EUR")
            .bodyValue(
                CurrencyFixtures.updateCurrencyRequest(
                    code = "EUR",
                    name = "Euro Updated",
                    symbol = "EUR",
                    decimalPlaces = 3,
                    symbolPosition = "after",
                ),
            )
            .exchange()
            .expectStatus().isOk

        webTestClient.get()
            .uri("/currencies/EUR")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.code").isEqualTo("EUR")
            .jsonPath("$.name").isEqualTo("Euro Updated")
            .jsonPath("$.symbol").isEqualTo("EUR")
            .jsonPath("$.decimalPlaces").isEqualTo(3)
            .jsonPath("$.symbolPosition").isEqualTo("after")
    }

    @Test
    fun `returns not found when updating an unknown currency`() {
        webTestClient.put()
            .uri("/currencies/EUR")
            .bodyValue(CurrencyFixtures.updateCurrencyRequest())
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .expectApiError(
                code = "not_found",
                message = "Currency not found",
                details = mapOf("code" to "EUR"),
            )
    }

    @Test
    fun `rejects code mismatch between path and body`() {
        createCurrencyOverHttp()

        webTestClient.put()
            .uri("/currencies/EUR")
            .bodyValue(CurrencyFixtures.updateCurrencyRequest(code = "usd"))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .expectApiError(
                code = "validation_error",
                message = "Path code must match body code",
                details = mapOf("pathCode" to "EUR", "bodyCode" to "USD"),
            )
    }

    @Test
    fun `preserves canonical uppercase code on update`() {
        createCurrencyOverHttp()

        webTestClient.put()
            .uri("/currencies/eur")
            .bodyValue(CurrencyFixtures.updateCurrencyRequest(code = "eur", name = "Euro Canonical"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.code").isEqualTo("EUR")
            .jsonPath("$.name").isEqualTo("Euro Canonical")
    }

    @Test
    fun `returns validation error for invalid update request`() {
        createCurrencyOverHttp()

        webTestClient.put()
            .uri("/currencies/EUR")
            .bodyValue(CurrencyFixtures.updateCurrencyRequest(decimalPlaces = -1))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .expectApiError(
                code = "validation_error",
                details = mapOf("decimalPlaces" to -1),
            )
    }

    @Test
    fun `returns bad request for malformed json`() {
        webTestClient.post()
            .uri("/currencies")
            .header("Content-Type", "application/json")
            .bodyValue("{\"code\":\"EUR\"")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .expectApiError(
                code = "bad_request",
                message = "Request body is malformed or invalid",
            )
    }

    @Test
    fun `returns bad request for missing required field`() {
        webTestClient.post()
            .uri("/currencies")
            .header("Content-Type", "application/json")
            .bodyValue(
                """
                {"code":"EUR","name":"Euro","symbol":"€","decimalPlaces":2}
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .expectApiError(
                code = "bad_request",
                message = "Request body is malformed or invalid",
            )
    }

    @Test
    fun `returns bad request for wrong field type`() {
        webTestClient.post()
            .uri("/currencies")
            .header("Content-Type", "application/json")
            .bodyValue(
                """
                {"code":"EUR","name":"Euro","symbol":"€","decimalPlaces":"two","symbolPosition":"before"}
                """.trimIndent(),
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .expectApiError(
                code = "bad_request",
                message = "Request body is malformed or invalid",
            )
    }

    @Test
    fun `returns a structured validation error`() {
        val request = CurrencyFixtures.createCurrencyRequest(decimalPlaces = -1)

        webTestClient.post()
            .uri("/currencies")
            .bodyValue(request)
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .expectApiError(
                code = "validation_error",
                details = mapOf("decimalPlaces" to -1),
            )
    }

    private fun createCurrencyOverHttp(request: CreateCurrencyRequest = CurrencyFixtures.createCurrencyRequest()) {
        webTestClient.post()
            .uri("/currencies")
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.code").isEqualTo(request.code.uppercase())
            .jsonPath("$.name").isEqualTo(request.name)
            .jsonPath("$.symbol").isEqualTo(request.symbol)
            .jsonPath("$.decimalPlaces").isEqualTo(request.decimalPlaces)
            .jsonPath("$.symbolPosition").isEqualTo(request.symbolPosition)
    }
}
