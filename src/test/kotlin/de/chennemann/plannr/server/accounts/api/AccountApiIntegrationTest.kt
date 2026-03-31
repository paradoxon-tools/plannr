package de.chennemann.plannr.server.accounts.api

import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.support.expectApiError
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AccountApiIntegrationTest : ApiIntegrationTest() {
    @BeforeEach
    fun setUp() {
        cleanDatabase("pockets", "accounts", "currencies")
    }

    @Test
    fun `creates and fetches account over http when currency already exists`() {
        createCurrencyOverHttp()

        webTestClient.post()
            .uri("/accounts")
            .bodyValue(AccountFixtures.createAccountRequest())
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.name").isEqualTo(AccountFixtures.DEFAULT_NAME)
            .jsonPath("$.institution").isEqualTo(AccountFixtures.DEFAULT_INSTITUTION)
            .jsonPath("$.currencyCode").isEqualTo(AccountFixtures.DEFAULT_CURRENCY_CODE)
            .jsonPath("$.weekendHandling").isEqualTo(AccountFixtures.DEFAULT_WEEKEND_HANDLING)
            .jsonPath("$.isArchived").isEqualTo(false)
            .jsonPath("$.id").exists()
            .jsonPath("$.createdAt").exists()
    }

    @Test
    fun `creates account and auto creates built in currency when missing`() {
        webTestClient.post()
            .uri("/accounts")
            .bodyValue(AccountFixtures.createAccountRequest(currencyCode = "eur"))
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.currencyCode").isEqualTo("EUR")

        webTestClient.get()
            .uri("/currencies/EUR")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.code").isEqualTo("EUR")
            .jsonPath("$.name").isEqualTo("Euro")
    }

    @Test
    fun `returns not found when account currency is unknown everywhere`() {
        webTestClient.post()
            .uri("/accounts")
            .bodyValue(AccountFixtures.createAccountRequest(currencyCode = "xyz"))
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .expectApiError(
                code = "not_found",
                message = "Currency not found",
                details = mapOf("code" to "XYZ"),
            )
    }

    @Test
    fun `gets account by id`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp(name = AccountFixtures.DEFAULT_NAME)

        webTestClient.get()
            .uri("/accounts/{id}", accountId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(accountId)
            .jsonPath("$.currencyCode").isEqualTo("EUR")
    }

    @Test
    fun `lists empty array when there are no accounts`() {
        webTestClient.get()
            .uri("/accounts")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json("[]")
    }

    @Test
    fun `lists accounts`() {
        createCurrencyOverHttp()
        createAccountOverHttp(name = "Main Account")
        createAccountOverHttp(name = "Savings")

        webTestClient.get()
            .uri("/accounts")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].name").isEqualTo("Main Account")
            .jsonPath("$[1].name").isEqualTo("Savings")
    }

    @Test
    fun `returns not found for unknown account`() {
        webTestClient.get()
            .uri("/accounts/acc_missing")
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
    fun `updates existing account`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp(name = AccountFixtures.DEFAULT_NAME)

        webTestClient.put()
            .uri("/accounts/{id}", accountId)
            .bodyValue(
                AccountFixtures.updateAccountRequest(
                    name = "Updated Account",
                    institution = "Updated Bank",
                    currencyCode = "EUR",
                    weekendHandling = "same_day",
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(accountId)
            .jsonPath("$.name").isEqualTo("Updated Account")
            .jsonPath("$.institution").isEqualTo("Updated Bank")
            .jsonPath("$.currencyCode").isEqualTo("EUR")
            .jsonPath("$.weekendHandling").isEqualTo("same_day")
    }

    @Test
    fun `updates account and auto creates built in currency when missing`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp(name = AccountFixtures.DEFAULT_NAME)

        webTestClient.put()
            .uri("/accounts/{id}", accountId)
            .bodyValue(AccountFixtures.updateAccountRequest(currencyCode = "usd"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currencyCode").isEqualTo("USD")

        webTestClient.get()
            .uri("/currencies/USD")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.code").isEqualTo("USD")
            .jsonPath("$.name").isEqualTo("US Dollar")
    }

    @Test
    fun `persists updated account values`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp(name = AccountFixtures.DEFAULT_NAME)

        webTestClient.put()
            .uri("/accounts/{id}", accountId)
            .bodyValue(AccountFixtures.updateAccountRequest(name = "Updated Account", institution = "Updated Bank", weekendHandling = "same_day"))
            .exchange()
            .expectStatus().isOk

        webTestClient.get()
            .uri("/accounts/{id}", accountId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.name").isEqualTo("Updated Account")
            .jsonPath("$.institution").isEqualTo("Updated Bank")
            .jsonPath("$.weekendHandling").isEqualTo("same_day")
    }

    @Test
    fun `returns not found when updating unknown account`() {
        createCurrencyOverHttp()

        webTestClient.put()
            .uri("/accounts/acc_missing")
            .bodyValue(AccountFixtures.updateAccountRequest())
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
    fun `returns not found when updated currency is unknown everywhere`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp(name = AccountFixtures.DEFAULT_NAME)

        webTestClient.put()
            .uri("/accounts/{id}", accountId)
            .bodyValue(AccountFixtures.updateAccountRequest(currencyCode = "xyz"))
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .expectApiError(
                code = "not_found",
                message = "Currency not found",
                details = mapOf("code" to "XYZ"),
            )
    }

    @Test
    fun `archives and unarchives account and propagates to pockets`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp(name = AccountFixtures.DEFAULT_NAME)
        val otherAccountId = createAccountOverHttp(name = "Savings")
        val pocketId = createPocketOverHttp(accountId = accountId, name = "Bills")
        val otherPocketId = createPocketOverHttp(accountId = otherAccountId, name = "Travel")

        webTestClient.post()
            .uri("/accounts/{id}/archive", accountId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(accountId)
            .jsonPath("$.isArchived").isEqualTo(true)

        webTestClient.get()
            .uri("/accounts/{id}", accountId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(true)

        webTestClient.get()
            .uri("/pockets/{id}", pocketId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(true)

        webTestClient.get()
            .uri("/pockets/{id}", otherPocketId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(false)

        webTestClient.post()
            .uri("/accounts/{id}/unarchive", accountId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(false)

        webTestClient.get()
            .uri("/pockets/{id}", pocketId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(false)
    }

    @Test
    fun `returns validation error for invalid update request`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp(name = AccountFixtures.DEFAULT_NAME)

        webTestClient.put()
            .uri("/accounts/{id}", accountId)
            .bodyValue(AccountFixtures.updateAccountRequest(name = "   "))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .expectApiError(
                code = "validation_error",
                message = "Account name must not be blank",
            )
    }

    @Test
    fun `returns not found when archiving unknown account`() {
        webTestClient.post()
            .uri("/accounts/acc_missing/archive")
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
    fun `returns not found when unarchiving unknown account`() {
        webTestClient.post()
            .uri("/accounts/acc_missing/unarchive")
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
    fun `returns validation error for invalid request`() {
        createCurrencyOverHttp()

        webTestClient.post()
            .uri("/accounts")
            .bodyValue(AccountFixtures.createAccountRequest(name = "   "))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .expectApiError(
                code = "validation_error",
                message = "Account name must not be blank",
            )
    }

    private fun createCurrencyOverHttp() {
        webTestClient.post()
            .uri("/currencies")
            .bodyValue(mapOf("code" to "EUR", "name" to "Euro", "symbol" to "€", "decimalPlaces" to 2, "symbolPosition" to "before"))
            .exchange()
            .expectStatus().isCreated
    }

    private fun createAccountOverHttp(name: String): String {
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

    private fun createPocketOverHttp(accountId: String, name: String): String {
        val response = webTestClient.post()
            .uri("/pockets")
            .bodyValue(
                mapOf(
                    "accountId" to accountId,
                    "name" to name,
                    "description" to "Pocket for $name",
                    "color" to 123456,
                    "isDefault" to false,
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody(Map::class.java)
            .returnResult()
            .responseBody!!

        return response["id"] as String
    }
}
