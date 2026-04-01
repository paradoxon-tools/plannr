package de.chennemann.plannr.server.recurringtransactions.api

import de.chennemann.plannr.server.accounts.api.AccountResponse
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.contracts.api.ContractResponse
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.partners.api.PartnerResponse
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import de.chennemann.plannr.server.pockets.api.PocketResponse
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.recurringtransactions.support.RecurringTransactionFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.support.expectApiError
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RecurringTransactionApiIntegrationTest : ApiIntegrationTest() {
    @BeforeEach fun setUp() { cleanDatabase("recurring_transactions", "contracts", "partners", "pockets", "accounts", "currencies") }

    @Test
    fun `creates gets lists updates and archives recurring transaction`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()
        val pocketId = createPocketOverHttp(accountId)
        val partnerId = createPartnerOverHttp()
        val contractId = createContractOverHttp(pocketId, partnerId)

        val id = webTestClient.post().uri("/recurring-transactions")
            .bodyValue(RecurringTransactionFixtures.createRequest(contractId = contractId, sourcePocketId = pocketId, partnerId = partnerId))
            .exchange().expectStatus().isCreated
            .expectBody(RecurringTransactionResponse::class.java).returnResult().responseBody!!.id

        webTestClient.get().uri("/recurring-transactions/{id}", id).exchange().expectStatus().isOk
            .expectBody().jsonPath("$.accountId").isEqualTo(accountId)

        webTestClient.get().uri { it.path("/recurring-transactions").queryParam("contractId", contractId).build() }.exchange().expectStatus().isOk
            .expectBody().jsonPath("$[0].id").isEqualTo(id)

        webTestClient.put().uri("/recurring-transactions/{id}", id)
            .bodyValue(RecurringTransactionFixtures.updateRequest(contractId = contractId, sourcePocketId = pocketId, partnerId = partnerId, title = "Updated Title"))
            .exchange().expectStatus().isOk
            .expectBody().jsonPath("$.title").isEqualTo("Updated Title")

        webTestClient.post().uri("/recurring-transactions/{id}/archive", id).exchange().expectStatus().isOk
            .expectBody().jsonPath("$.isArchived").isEqualTo(true)

        webTestClient.post().uri("/recurring-transactions/{id}/unarchive", id).exchange().expectStatus().isOk
            .expectBody().jsonPath("$.isArchived").isEqualTo(false)
    }

    @Test
    fun `supports parallel and effective from update modes`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()
        val pocketId = createPocketOverHttp(accountId)
        val contractId = createContractOverHttp(pocketId, null)
        val id = webTestClient.post().uri("/recurring-transactions")
            .bodyValue(RecurringTransactionFixtures.createRequest(contractId = contractId, sourcePocketId = pocketId, partnerId = null))
            .exchange().expectStatus().isCreated
            .expectBody(RecurringTransactionResponse::class.java).returnResult().responseBody!!.id

        val parallelId = webTestClient.put().uri("/recurring-transactions/{id}", id)
            .bodyValue(RecurringTransactionFixtures.updateRequest(updateMode = "parallel", contractId = contractId, sourcePocketId = pocketId, partnerId = null, title = "Parallel"))
            .exchange().expectStatus().isOk
            .expectBody(RecurringTransactionResponse::class.java).returnResult().responseBody!!.id

        webTestClient.put().uri("/recurring-transactions/{id}", id)
            .bodyValue(RecurringTransactionFixtures.updateRequest(updateMode = "effective_from", effectiveFromDate = "2024-06-01", contractId = contractId, sourcePocketId = pocketId, partnerId = null, title = "Future"))
            .exchange().expectStatus().isOk
            .expectBody().jsonPath("$.previousVersionId").isEqualTo(id)

        webTestClient.get().uri { it.path("/recurring-transactions").queryParam("archived", true).build() }.exchange().expectStatus().isOk
            .expectBody().jsonPath("$[?(@.id=='$id')].id").exists()

        webTestClient.get().uri("/recurring-transactions/{id}", parallelId).exchange().expectStatus().isOk
    }

    @Test
    fun `returns validation error for incompatible contract context`() {
        createCurrencyOverHttp()
        val accountId = createAccountOverHttp()
        val pocketId = createPocketOverHttp(accountId)
        val otherPocketId = createPocketOverHttp(accountId, "Travel")
        val contractId = createContractOverHttp(pocketId, null)

        webTestClient.post().uri("/recurring-transactions")
            .bodyValue(RecurringTransactionFixtures.createRequest(contractId = contractId, sourcePocketId = otherPocketId, partnerId = null))
            .exchange().expectStatus().isEqualTo(422)
            .expectBody().expectApiError("validation_error", "Recurring transaction must reference the contract pocket as source or destination")
    }

    private fun createCurrencyOverHttp() {
        webTestClient.post().uri("/currencies").bodyValue(mapOf("code" to "EUR", "name" to "Euro", "symbol" to "€", "decimalPlaces" to 2, "symbolPosition" to "before")).exchange().expectStatus().isCreated
    }
    private fun createAccountOverHttp(name: String = AccountFixtures.DEFAULT_NAME): String = webTestClient.post().uri("/accounts").bodyValue(AccountFixtures.createAccountRequest(name = name)).exchange().expectStatus().isCreated.expectBody(AccountResponse::class.java).returnResult().responseBody!!.id
    private fun createPocketOverHttp(accountId: String, name: String = PocketFixtures.DEFAULT_NAME): String = webTestClient.post().uri("/pockets").bodyValue(PocketFixtures.createPocketRequest(accountId = accountId, name = name)).exchange().expectStatus().isCreated.expectBody(PocketResponse::class.java).returnResult().responseBody!!.id
    private fun createPartnerOverHttp(name: String = PartnerFixtures.DEFAULT_NAME): String = webTestClient.post().uri("/partners").bodyValue(PartnerFixtures.createPartnerRequest(name = name)).exchange().expectStatus().isCreated.expectBody(PartnerResponse::class.java).returnResult().responseBody!!.id
    private fun createContractOverHttp(pocketId: String, partnerId: String?): String = webTestClient.post().uri("/contracts").bodyValue(ContractFixtures.createContractRequest(pocketId = pocketId, partnerId = partnerId)).exchange().expectStatus().isCreated.expectBody(ContractResponse::class.java).returnResult().responseBody!!.id
}
