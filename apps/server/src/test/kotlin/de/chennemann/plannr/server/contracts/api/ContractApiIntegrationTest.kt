package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.accounts.api.AccountResponse
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.partners.api.PartnerResponse
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import de.chennemann.plannr.server.pockets.api.PocketResponse
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.support.expectApiError
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContractApiIntegrationTest : ApiIntegrationTest() {
    @BeforeEach
    fun setUp() {
        cleanDatabase("contracts", "partners", "pockets", "accounts", "currencies")
    }

    @Test
    fun `creates and fetches contract over http`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()
        val pocketId = createPocketOverHttp(accountId)
        val partnerId = createPartnerOverHttp()

        webTestClient.post()
            .uri("/contracts")
            .bodyValue(ContractFixtures.createContractRequest(pocketId = pocketId, partnerId = partnerId))
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.accountId").isEqualTo(accountId)
            .jsonPath("$.pocketId").isEqualTo(pocketId)
            .jsonPath("$.partnerId").isEqualTo(partnerId)
            .jsonPath("$.name").isEqualTo(ContractFixtures.DEFAULT_NAME)
            .jsonPath("$.startDate").isEqualTo(ContractFixtures.DEFAULT_START_DATE)
            .jsonPath("$.endDate").isEqualTo(ContractFixtures.DEFAULT_END_DATE)
            .jsonPath("$.notes").isEqualTo(ContractFixtures.DEFAULT_NOTES)
            .jsonPath("$.isArchived").isEqualTo(false)
            .jsonPath("$.id").exists()
            .jsonPath("$.createdAt").exists()
    }

    @Test
    fun `lists contracts with filters`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()
        val otherAccountId = createAccountOverHttp(name = "Savings")
        val pocketId = createPocketOverHttp(accountId, name = "Bills")
        val otherPocketId = createPocketOverHttp(otherAccountId, name = "Travel")
        val firstId = createContractOverHttp(pocketId = pocketId, partnerId = null)
        val archivedId = createContractOverHttp(pocketId = otherPocketId, partnerId = null, name = "Archived Contract")
        archiveContractOverHttp(archivedId)

        webTestClient.get()
            .uri { builder -> builder.path("/contracts").queryParam("accountId", accountId).build() }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(firstId)

        webTestClient.get()
            .uri { builder -> builder.path("/contracts").queryParam("archived", true).build() }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(archivedId)
            .jsonPath("$[0].isArchived").isEqualTo(true)
    }

    @Test
    fun `updates existing contract`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()
        val otherAccountId = createAccountOverHttp(name = "Savings")
        val pocketId = createPocketOverHttp(accountId)
        val otherPocketId = createPocketOverHttp(otherAccountId, name = "Rent")
        val partnerId = createPartnerOverHttp()
        val otherPartnerId = createPartnerOverHttp(name = "Telecom GmbH")
        val contractId = createContractOverHttp(pocketId = pocketId, partnerId = partnerId)

        webTestClient.put()
            .uri("/contracts/{id}", contractId)
            .bodyValue(
                ContractFixtures.updateContractRequest(
                    pocketId = otherPocketId,
                    partnerId = otherPartnerId,
                    name = "Updated Contract",
                    startDate = "2024-02-01",
                    endDate = null,
                    notes = null,
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(contractId)
            .jsonPath("$.accountId").isEqualTo(otherAccountId)
            .jsonPath("$.pocketId").isEqualTo(otherPocketId)
            .jsonPath("$.partnerId").isEqualTo(otherPartnerId)
            .jsonPath("$.name").isEqualTo("Updated Contract")
            .jsonPath("$.endDate").isEmpty
            .jsonPath("$.notes").isEmpty
    }

    @Test
    fun `archives and unarchives contract`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()
        val pocketId = createPocketOverHttp(accountId)
        val contractId = createContractOverHttp(pocketId = pocketId, partnerId = null)

        webTestClient.post()
            .uri("/contracts/{id}/archive", contractId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(true)

        webTestClient.post()
            .uri("/contracts/{id}/unarchive", contractId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(false)
    }

    @Test
    fun `returns conflict when pocket already has a contract`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()
        val pocketId = createPocketOverHttp(accountId)
        createContractOverHttp(pocketId = pocketId, partnerId = null)

        webTestClient.post()
            .uri("/contracts")
            .bodyValue(ContractFixtures.createContractRequest(pocketId = pocketId, partnerId = null))
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .expectApiError(
                code = "conflict",
                message = "Contract already exists for pocket",
                details = mapOf("pocketId" to pocketId),
            )
    }

    @Test
    fun `returns validation error for invalid create request`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()
        val pocketId = createPocketOverHttp(accountId)

        webTestClient.post()
            .uri("/contracts")
            .bodyValue(ContractFixtures.createContractRequest(pocketId = pocketId, partnerId = null, startDate = "2024/01/01"))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .expectApiError(
                code = "validation_error",
                message = "Contract start date must be a plain date",
            )
    }

    @Test
    fun `returns not found for unknown contract`() {
        webTestClient.get()
            .uri("/contracts/con_missing")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .expectApiError(
                code = "not_found",
                message = "Contract not found",
                details = mapOf("id" to "con_missing"),
            )
    }

    private fun createCurrencyOverHttp() {
        webTestClient.post().uri("/currencies")
            .bodyValue(mapOf("code" to "EUR", "name" to "Euro", "symbol" to "€", "decimalPlaces" to 2, "symbolPosition" to "before"))
            .exchange()
            .expectStatus().isCreated
    }

    private fun createAccountOverHttp(name: String = AccountFixtures.DEFAULT_NAME): String {
        val response = webTestClient.post().uri("/accounts")
            .bodyValue(AccountFixtures.createAccountRequest(name = name))
            .exchange()
            .expectStatus().isCreated
            .expectBody(AccountResponse::class.java)
            .returnResult().responseBody!!
        return response.id
    }

    private fun createPocketOverHttp(accountId: String, name: String = PocketFixtures.DEFAULT_NAME): String {
        val response = webTestClient.post().uri("/pockets")
            .bodyValue(PocketFixtures.createPocketRequest(accountId = accountId, name = name))
            .exchange()
            .expectStatus().isCreated
            .expectBody(PocketResponse::class.java)
            .returnResult().responseBody!!
        return response.id
    }

    private fun createPartnerOverHttp(name: String = PartnerFixtures.DEFAULT_NAME): String {
        val response = webTestClient.post().uri("/partners")
            .bodyValue(PartnerFixtures.createPartnerRequest(name = name))
            .exchange()
            .expectStatus().isCreated
            .expectBody(PartnerResponse::class.java)
            .returnResult().responseBody!!
        return response.id
    }

    private fun createContractOverHttp(
        pocketId: String,
        partnerId: String?,
        name: String = ContractFixtures.DEFAULT_NAME,
    ): String {
        val response = webTestClient.post().uri("/contracts")
            .bodyValue(ContractFixtures.createContractRequest(pocketId = pocketId, partnerId = partnerId, name = name))
            .exchange()
            .expectStatus().isCreated
            .expectBody(ContractResponse::class.java)
            .returnResult().responseBody!!
        return response.id
    }

    private fun archiveContractOverHttp(contractId: String) {
        webTestClient.post().uri("/contracts/{id}/archive", contractId)
            .exchange()
            .expectStatus().isOk
    }
}
