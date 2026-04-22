package de.chennemann.plannr.server.transactions.api

import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.accounts.service.CreateAccountCommand
import de.chennemann.plannr.server.pockets.service.CreatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import kotlin.test.assertEquals

class ModifyRecurringOccurrenceApiIntegrationTest : ApiIntegrationTest() {
    @Autowired lateinit var databaseClient: DatabaseClient
    @Autowired lateinit var accountService: AccountService
    @Autowired lateinit var pocketService: PocketService

    @BeforeEach
    fun setUp() {
        cleanDatabase(
            "account_transaction_feed",
            "pocket_transaction_feed",
            "transactions",
            "pocket_query",
            "account_query",
            "recurring_transactions",
            "contracts",
            "partners",
            "pockets",
            "accounts",
            "currencies",
        )
        insertCurrency("EUR")
    }

    @Test
    fun `modify recurring occurrence endpoint hides original and projects child`() = runBlocking {
        val account = accountService.create(CreateAccountCommand("Main account", "Demo Bank", "EUR", "NO_SHIFT"))
        val pocket = pocketService.create(CreatePocketCommand(account.id, "Wallet", null, 123, true))
        insertRecurringRootTransaction(account.id, pocket.id)

        webTestClient.post()
            .uri("/transactions/txn_root/modify-recurring-occurrence")
            .bodyValue(
                mapOf(
                    "type" to "EXPENSE",
                    "status" to "CLEARED",
                    "transactionDate" to "2024-04-12",
                    "amount" to 7000,
                    "currencyCode" to "EUR",
                    "exchangeRate" to null,
                    "destinationAmount" to null,
                    "description" to "Updated internet",
                    "partnerId" to null,
                    "sourcePocketId" to pocket.id,
                    "destinationPocketId" to null,
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.description").isEqualTo("Updated internet")
            .jsonPath("$.sourcePocketId").isEqualTo(pocket.id)

        val childId = querySingleValue("SELECT id AS value FROM transactions WHERE parent_transaction_id = 'txn_root'")
        assertEquals(childId, querySingleValue("SELECT modified_by_id AS value FROM transactions WHERE id = 'txn_root'"))
        assertEquals("txn_root", querySingleValue("SELECT parent_transaction_id AS value FROM transactions WHERE id = '$childId'"))
        assertEquals("rtx_123", querySingleValue("SELECT recurring_transaction_id AS value FROM transactions WHERE id = '$childId'"))

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].transactionId").isEqualTo(childId)
            .jsonPath("$.items[0].description").isEqualTo("Updated internet")
    }

    private suspend fun querySingleValue(sql: String): String =
        databaseClient.sql(sql)
            .fetch()
            .one()
            .awaitSingle()
            .getValue("value") as String

    private fun insertCurrency(code: String) = runBlocking {
        databaseClient.sql(
            """
            INSERT INTO currencies (code, name, symbol, decimal_places, symbol_position)
            VALUES (:code, 'Euro', '€', 2, 'before')
            """.trimIndent(),
        )
            .bind("code", code)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    private fun insertRecurringRootTransaction(accountId: String, pocketId: String) = runBlocking {
        databaseClient.sql(
            """
            INSERT INTO transactions (
                id, pocket_id, type, status, transaction_date, amount, currency_code, exchange_rate,
                destination_amount, description, partner_id, source_pocket_id, destination_pocket_id,
                parent_transaction_id, recurring_transaction_id, modified_by_id, transaction_origin, is_archived, created_at
            ) VALUES (
                'txn_root', :pocketId, 'EXPENSE', 'PENDING', '2024-04-10', 4999, 'EUR', NULL,
                NULL, 'Monthly internet', NULL, NULL, NULL,
                NULL, 'rtx_123', NULL, 'RECURRING_MATERIALIZED', FALSE, 1
            )
            """.trimIndent(),
        )
            .bind("pocketId", pocketId)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
