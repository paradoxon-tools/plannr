package de.chennemann.plannr.server

import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.accounts.service.CreateAccountCommand
import de.chennemann.plannr.server.pockets.service.CreatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.transactions.recurring.usecases.RecurringTransactionMaterializer
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.support.expectApiError
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient

class ApiPhase9IntegrationTest : ApiIntegrationTest() {
    @Autowired lateinit var databaseClient: DatabaseClient
    @Autowired lateinit var accountService: AccountService
    @Autowired lateinit var pocketService: PocketService
    @Autowired lateinit var recurringTransactionMaterializer: RecurringTransactionMaterializer
    @BeforeEach
    fun setUp() {
        cleanDatabase(
            "projection_dirty_scope",
            "account_future_transaction_feed",
            "pocket_future_transaction_feed",
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
    fun `transaction api accepts pocketId for non transfer and exposes linkage fields`() = runBlocking {
        val account = accountService.create(CreateAccountCommand("Main account", "Demo Bank", "EUR", "NO_SHIFT"))
        val pocket = pocketService.create(CreatePocketCommand(account.id, "Wallet", null, 123, true))

        webTestClient.post()
            .uri("/transactions")
            .bodyValue(
                mapOf(
                    "type" to "EXPENSE",
                    "status" to "CLEARED",
                    "transactionDate" to "2026-04-10",
                    "amount" to 100,
                    "currencyCode" to "EUR",
                    "exchangeRate" to null,
                    "destinationAmount" to null,
                    "description" to "Groceries",
                    "partnerId" to null,
                    "pocketId" to pocket.id,
                    "sourcePocketId" to null,
                    "destinationPocketId" to null,
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.accountId").isEqualTo(account.id)
            .jsonPath("$.pocketId").isEqualTo(pocket.id)
            .jsonPath("$.sourcePocketId").isEqualTo(pocket.id)
            .jsonPath("$.destinationPocketId").doesNotExist()
            .jsonPath("$.parentTransactionId").doesNotExist()
            .jsonPath("$.recurringTransactionId").doesNotExist()
            .jsonPath("$.modifiedById").doesNotExist()
            .jsonPath("$.transactionOrigin").isEqualTo("MANUAL")
    }

    @Test
    fun `transaction api accepts transfer using source and destination pockets only`() = runBlocking {
        val account = accountService.create(CreateAccountCommand("Main account", "Demo Bank", "EUR", "NO_SHIFT"))
        val sourcePocket = pocketService.create(CreatePocketCommand(account.id, "Checking", null, 123, true))
        val destinationPocket = pocketService.create(CreatePocketCommand(account.id, "Savings", null, 456, false))

        webTestClient.post()
            .uri("/transactions")
            .bodyValue(
                mapOf(
                    "type" to "TRANSFER",
                    "status" to "CLEARED",
                    "transactionDate" to "2026-04-11",
                    "amount" to 50,
                    "currencyCode" to "EUR",
                    "exchangeRate" to null,
                    "destinationAmount" to null,
                    "description" to "Move",
                    "partnerId" to null,
                    "pocketId" to null,
                    "sourcePocketId" to sourcePocket.id,
                    "destinationPocketId" to destinationPocket.id,
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.pocketId").doesNotExist()
            .jsonPath("$.sourcePocketId").isEqualTo(sourcePocket.id)
            .jsonPath("$.destinationPocketId").isEqualTo(destinationPocket.id)
    }

    @Test
    fun `transaction api supports canonical updates and archive lifecycle`() = runBlocking {
        val account = accountService.create(CreateAccountCommand("Main account", "Demo Bank", "EUR", "NO_SHIFT"))
        val pocket = pocketService.create(CreatePocketCommand(account.id, "Wallet", null, 123, true))

        webTestClient.post()
            .uri("/transactions")
            .bodyValue(
                mapOf(
                    "type" to "EXPENSE",
                    "status" to "RECONCILED",
                    "transactionDate" to "2026-04-10",
                    "amount" to 100,
                    "currencyCode" to "EUR",
                    "exchangeRate" to null,
                    "destinationAmount" to null,
                    "description" to "Groceries",
                    "partnerId" to null,
                    "pocketId" to pocket.id,
                    "sourcePocketId" to null,
                    "destinationPocketId" to null,
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.status").isEqualTo("RECONCILED")

        val transactionId = querySingleValue("SELECT id AS value FROM transactions ORDER BY created_at ASC LIMIT 1")

        webTestClient.put()
            .uri("/transactions/$transactionId")
            .bodyValue(
                mapOf(
                    "type" to "EXPENSE",
                    "status" to "CLEARED",
                    "transactionDate" to "2026-04-11",
                    "amount" to 150,
                    "currencyCode" to "EUR",
                    "exchangeRate" to null,
                    "destinationAmount" to null,
                    "description" to "Groceries updated",
                    "partnerId" to null,
                    "pocketId" to pocket.id,
                    "sourcePocketId" to null,
                    "destinationPocketId" to null,
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("CLEARED")
            .jsonPath("$.transactionDate").isEqualTo("2026-04-11")
            .jsonPath("$.description").isEqualTo("Groceries updated")

        webTestClient.post().uri("/transactions/$transactionId/archive").exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.isArchived").isEqualTo(true)

        webTestClient.post().uri("/transactions/$transactionId/unarchive").exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.isArchived").isEqualTo(false)
    }

    @Test
    fun `modify recurring occurrence endpoint exposes modification linkage`() = runBlocking {
        val account = accountService.create(CreateAccountCommand("Main account", "Demo Bank", "EUR", "NO_SHIFT"))
        val pocket = pocketService.create(CreatePocketCommand(account.id, "Wallet", null, 123, true))
        insertRecurringRootTransaction(pocket.id)

        webTestClient.post()
            .uri("/transactions/txn_root/modify-recurring-occurrence")
            .bodyValue(
                mapOf(
                    "type" to "EXPENSE",
                    "status" to "CLEARED",
                    "transactionDate" to "2026-04-12",
                    "amount" to 150,
                    "currencyCode" to "EUR",
                    "exchangeRate" to null,
                    "destinationAmount" to null,
                    "description" to "Updated",
                    "partnerId" to null,
                    "pocketId" to pocket.id,
                    "sourcePocketId" to null,
                    "destinationPocketId" to null,
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.parentTransactionId").isEqualTo("txn_root")
            .jsonPath("$.recurringTransactionId").isEqualTo("rtx_123")
            .jsonPath("$.transactionOrigin").isEqualTo("RECURRING_MODIFICATION")
    }

    @Test
    fun `recurring api supports yearly and max recurrence count normalization`() = runBlocking {
        val account = accountService.create(CreateAccountCommand("Main account", "Demo Bank", "EUR", "NO_SHIFT"))
        val pocket = pocketService.create(CreatePocketCommand(account.id, "Bills", null, 123, true))

        webTestClient.post()
            .uri("/transactions/recurring")
            .bodyValue(
                mapOf(
                    "contractId" to null,
                    "sourcePocketId" to pocket.id,
                    "destinationPocketId" to null,
                    "partnerId" to null,
                    "title" to "Insurance",
                    "description" to "Yearly insurance",
                    "amount" to 100,
                    "currencyCode" to "EUR",
                    "transactionType" to "EXPENSE",
                    "firstOccurrenceDate" to "2024-02-29",
                    "finalOccurrenceDate" to null,
                    "recurrenceType" to "YEARLY",
                    "skipCount" to 0,
                    "daysOfWeek" to null,
                    "weeksOfMonth" to null,
                    "daysOfMonth" to listOf(29),
                    "monthsOfYear" to listOf(2),
                    "maxRecurrenceCount" to 2,
                ),
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.recurrenceType").isEqualTo("YEARLY")
            .jsonPath("$.finalOccurrenceDate").isEqualTo("2025-02-28")
    }

    @Test
    fun `recurring update api uses new_version without effectiveFromDate`() = runBlocking {
        val account = accountService.create(CreateAccountCommand("Main account", "Demo Bank", "EUR", "NO_SHIFT"))
        val pocket = pocketService.create(CreatePocketCommand(account.id, "Bills", null, 123, true))
        webTestClient.post()
            .uri("/transactions/recurring")
            .bodyValue(
                mapOf(
                    "contractId" to null,
                    "sourcePocketId" to pocket.id,
                    "destinationPocketId" to null,
                    "partnerId" to null,
                    "title" to "Rent",
                    "description" to "Rent",
                    "amount" to 100,
                    "currencyCode" to "EUR",
                    "transactionType" to "EXPENSE",
                    "firstOccurrenceDate" to "2024-01-15",
                    "finalOccurrenceDate" to null,
                    "recurrenceType" to "MONTHLY",
                    "skipCount" to 0,
                    "daysOfWeek" to null,
                    "weeksOfMonth" to null,
                    "daysOfMonth" to listOf(15),
                    "monthsOfYear" to null,
                    "maxRecurrenceCount" to null,
                ),
            )
            .exchange()
            .expectStatus().isCreated

        val storedId = querySingleValue("SELECT id AS value FROM recurring_transactions ORDER BY created_at ASC LIMIT 1")

        webTestClient.put()
            .uri("/transactions/recurring/$storedId")
            .bodyValue(
                mapOf(
                    "updateMode" to "new_version",
                    "contractId" to null,
                    "sourcePocketId" to pocket.id,
                    "destinationPocketId" to null,
                    "partnerId" to null,
                    "title" to "Rent v2",
                    "description" to "Rent v2",
                    "amount" to 120,
                    "currencyCode" to "EUR",
                    "transactionType" to "EXPENSE",
                    "firstOccurrenceDate" to "2024-06-15",
                    "finalOccurrenceDate" to null,
                    "recurrenceType" to "MONTHLY",
                    "skipCount" to 0,
                    "daysOfWeek" to null,
                    "weeksOfMonth" to null,
                    "daysOfMonth" to listOf(15),
                    "monthsOfYear" to null,
                    "maxRecurrenceCount" to null,
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.previousVersionId").isEqualTo(storedId)
            .jsonPath("$.firstOccurrenceDate").isEqualTo("2024-06-15")

        webTestClient.put()
            .uri("/transactions/recurring/$storedId")
            .bodyValue(
                mapOf(
                    "updateMode" to "parallel",
                    "contractId" to null,
                    "sourcePocketId" to pocket.id,
                    "destinationPocketId" to null,
                    "partnerId" to null,
                    "title" to "Invalid",
                    "description" to null,
                    "amount" to 120,
                    "currencyCode" to "EUR",
                    "transactionType" to "EXPENSE",
                    "firstOccurrenceDate" to "2024-07-15",
                    "finalOccurrenceDate" to null,
                    "recurrenceType" to "MONTHLY",
                    "skipCount" to 0,
                    "daysOfWeek" to null,
                    "weeksOfMonth" to null,
                    "daysOfMonth" to listOf(15),
                    "monthsOfYear" to null,
                    "maxRecurrenceCount" to null,
                ),
            )
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .expectApiError("validation_error", "Recurring transaction update mode is invalid")
    }

    @Test
    fun `archived recurring transaction is skipped by materialization`() = runBlocking {
        val account = accountService.create(CreateAccountCommand("Main account", "Demo Bank", "EUR", "MOVE_BEFORE"))
        val pocket = pocketService.create(CreatePocketCommand(account.id, "Bills", null, 123, true))

        webTestClient.post()
            .uri("/transactions/recurring")
            .bodyValue(
                mapOf(
                    "contractId" to null,
                    "sourcePocketId" to pocket.id,
                    "destinationPocketId" to null,
                    "partnerId" to null,
                    "title" to "Archived rent",
                    "description" to "Archived rent",
                    "amount" to 100,
                    "currencyCode" to "EUR",
                    "transactionType" to "EXPENSE",
                    "firstOccurrenceDate" to "2024-04-13",
                    "finalOccurrenceDate" to "2024-04-13",
                    "recurrenceType" to "NONE",
                    "skipCount" to 0,
                    "daysOfWeek" to null,
                    "weeksOfMonth" to null,
                    "daysOfMonth" to null,
                    "monthsOfYear" to null,
                    "maxRecurrenceCount" to null,
                ),
            )
            .exchange()
            .expectStatus().isCreated

        val recurringId = querySingleValue("SELECT id AS value FROM recurring_transactions ORDER BY created_at ASC LIMIT 1")
        webTestClient.post().uri("/transactions/recurring/$recurringId/archive").exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.archived").isEqualTo(true)

        recurringTransactionMaterializer.materializeAll()

        val count = queryLong("SELECT COUNT(*) AS value FROM transactions WHERE recurring_transaction_id = '$recurringId'")
        assert(count == 0L)
    }

    private fun insertCurrency(code: String) = runBlocking {
        databaseClient.sql(
            """
            INSERT INTO currencies (code, name, symbol, decimal_places, symbol_position)
            VALUES (:code, 'Euro', '€', 2, 'before')
            """.trimIndent(),
        ).bind("code", code).fetch().rowsUpdated().awaitSingle()
    }

    private fun insertRecurringRootTransaction(pocketId: String) = runBlocking {
        databaseClient.sql(
            """
            INSERT INTO transactions (
                id, pocket_id, type, status, transaction_date, amount, currency_code, exchange_rate,
                destination_amount, description, partner_id, source_pocket_id, destination_pocket_id,
                parent_transaction_id, recurring_transaction_id, modified_by_id, transaction_origin, is_archived, created_at
            ) VALUES (
                'txn_root', :pocketId, 'EXPENSE', 'PENDING', '2026-04-10', 100, 'EUR', NULL,
                NULL, 'Original', NULL, NULL, NULL,
                NULL, 'rtx_123', NULL, 'RECURRING_MATERIALIZED', FALSE, 1
            )
            """.trimIndent(),
        ).bind("pocketId", pocketId).fetch().rowsUpdated().awaitSingle()
    }

    private suspend fun querySingleValue(sql: String): String =
        databaseClient.sql(sql).fetch().one().awaitSingle().getValue("value") as String

    private suspend fun queryLong(sql: String): Long =
        (databaseClient.sql(sql).fetch().one().awaitSingle().getValue("value") as Number).toLong()
}
