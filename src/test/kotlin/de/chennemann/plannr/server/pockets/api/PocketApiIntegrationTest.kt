package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.accounts.api.AccountResponse
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.support.expectApiError
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PocketApiIntegrationTest : ApiIntegrationTest() {
    @BeforeEach
    fun setUp() {
        cleanDatabase("pockets", "accounts", "currencies")
    }

    @Test
    fun `creates and fetches pocket over http`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()

        webTestClient.post()
            .uri("/pockets")
            .bodyValue(PocketFixtures.createPocketRequest(accountId = accountId))
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.accountId").isEqualTo(accountId)
            .jsonPath("$.name").isEqualTo(PocketFixtures.DEFAULT_NAME)
            .jsonPath("$.description").isEqualTo(PocketFixtures.DEFAULT_DESCRIPTION)
            .jsonPath("$.color").isEqualTo(PocketFixtures.DEFAULT_COLOR)
            .jsonPath("$.isDefault").isEqualTo(false)
            .jsonPath("$.isArchived").isEqualTo(false)
            .jsonPath("$.id").exists()
            .jsonPath("$.createdAt").exists()
    }

    @Test
    fun `returns not found when creating pocket for unknown account`() {
        webTestClient.post()
            .uri("/pockets")
            .bodyValue(PocketFixtures.createPocketRequest(accountId = "acc_missing"))
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .expectApiError(
                code = "not_found",
                message = "Account not found",
                details = mapOf("id" to "acc_missing"),
            )
    }

    @Test
    fun `gets pocket by id`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()
        val pocketId = createPocketOverHttp(accountId = accountId)

        webTestClient.get()
            .uri("/pockets/{id}", pocketId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(pocketId)
            .jsonPath("$.accountId").isEqualTo(accountId)
    }

    @Test
    fun `lists pockets with filters`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()
        val secondAccountId = createAccountOverHttp(name = "Savings")
        createPocketOverHttp(accountId = accountId, name = "Bills")
        val archivedPocketId = createPocketOverHttp(accountId = accountId, name = "Savings Bucket")
        createPocketOverHttp(accountId = secondAccountId, name = "Travel")
        archivePocketOverHttp(archivedPocketId)

        webTestClient.get()
            .uri { builder -> builder.path("/pockets").queryParam("accountId", accountId).queryParam("archived", true).build() }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(archivedPocketId)
            .jsonPath("$[0].isArchived").isEqualTo(true)
    }

    @Test
    fun `updates existing pocket`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()
        val secondAccountId = createAccountOverHttp(name = "Savings")
        val pocketId = createPocketOverHttp(accountId = accountId)

        webTestClient.put()
            .uri("/pockets/{id}", pocketId)
            .bodyValue(
                PocketFixtures.updatePocketRequest(
                    accountId = secondAccountId,
                    name = "Updated Pocket",
                    description = null,
                    color = 42,
                    isDefault = true,
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(pocketId)
            .jsonPath("$.accountId").isEqualTo(secondAccountId)
            .jsonPath("$.name").isEqualTo("Updated Pocket")
            .jsonPath("$.description").isEmpty
            .jsonPath("$.color").isEqualTo(42)
            .jsonPath("$.isDefault").isEqualTo(true)
    }

    @Test
    fun `archives and unarchives pocket`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()
        val pocketId = createPocketOverHttp(accountId = accountId)

        webTestClient.post()
            .uri("/pockets/{id}/archive", pocketId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(true)

        webTestClient.post()
            .uri("/pockets/{id}/unarchive", pocketId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(false)
    }

    @Test
    fun `returns validation error for invalid create request`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()

        webTestClient.post()
            .uri("/pockets")
            .bodyValue(PocketFixtures.createPocketRequest(accountId = accountId, name = "   "))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .expectApiError(
                code = "validation_error",
                message = "Pocket name must not be blank",
            )
    }

    @Test
    fun `returns not found for unknown pocket`() {
        webTestClient.get()
            .uri("/pockets/poc_missing")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .expectApiError(
                code = "not_found",
                message = "Pocket not found",
                details = mapOf("id" to "poc_missing"),
            )
    }

    private fun createCurrencyOverHttp() {
        webTestClient.post()
            .uri("/currencies")
            .bodyValue(mapOf("code" to "EUR", "name" to "Euro", "symbol" to "€", "decimalPlaces" to 2, "symbolPosition" to "before"))
            .exchange()
            .expectStatus().isCreated
    }

    private fun createAccountOverHttp(name: String = AccountFixtures.DEFAULT_NAME): String {
        val response = webTestClient.post()
            .uri("/accounts")
            .bodyValue(AccountFixtures.createAccountRequest(name = name))
            .exchange()
            .expectStatus().isCreated
            .expectBody(AccountResponse::class.java)
            .returnResult()
            .responseBody!!

        return response.id
    }

    private fun createPocketOverHttp(accountId: String, name: String = PocketFixtures.DEFAULT_NAME): String {
        val response = webTestClient.post()
            .uri("/pockets")
            .bodyValue(PocketFixtures.createPocketRequest(accountId = accountId, name = name))
            .exchange()
            .expectStatus().isCreated
            .expectBody(PocketResponse::class.java)
            .returnResult()
            .responseBody!!

        return response.id
    }

    private fun archivePocketOverHttp(pocketId: String) {
        webTestClient.post()
            .uri("/pockets/{id}/archive", pocketId)
            .exchange()
            .expectStatus().isOk
    }
}
