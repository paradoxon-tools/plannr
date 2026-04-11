package de.chennemann.plannr.server.query

import de.chennemann.plannr.server.accounts.usecases.CreateAccount
import de.chennemann.plannr.server.contracts.usecases.CreateContract
import de.chennemann.plannr.server.pockets.usecases.CreatePocket
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.transactions.usecases.CreateTransaction
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient

class FutureAndContractTransactionQueryIntegrationTest : ApiIntegrationTest() {
    @Autowired lateinit var databaseClient: DatabaseClient
    @Autowired lateinit var createAccount: CreateAccount
    @Autowired lateinit var createPocket: CreatePocket
    @Autowired lateinit var createContract: CreateContract
    @Autowired lateinit var createTransaction: CreateTransaction

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
    fun `historical and future feeds are separated and contract queries use pocket projections`() = runBlocking {
        val account = createAccount(CreateAccount.Command("Main account", "Demo Bank", "EUR", "NO_SHIFT"))
        val contractPocket = createPocket(CreatePocket.Command(account.id, "Bills", null, 123, true))
        val otherPocket = createPocket(CreatePocket.Command(account.id, "Savings", null, 456, false))
        val contract = createContract(CreateContract.Command(contractPocket.id, null, "Rent", "2024-01-01", null, null))

        createTransaction(CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-10", 100, "EUR", null, null, "Past expense", null, contractPocket.id, null))
        createTransaction(CreateTransaction.Command("EXPENSE", "PENDING", "2026-04-20", 200, "EUR", null, null, "Future expense", null, contractPocket.id, null))
        createTransaction(CreateTransaction.Command("TRANSFER", "PENDING", "2026-04-21", 50, "EUR", null, null, "Future transfer", null, contractPocket.id, otherPocket.id))

        webTestClient.get()
            .uri("/query/pockets/${contractPocket.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].description").isEqualTo("Past expense")
            .jsonPath("$.items[0].contractId").isEqualTo(contract.id)

        webTestClient.get()
            .uri("/query/contracts/${contract.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].description").isEqualTo("Past expense")
            .jsonPath("$.items[0].contractId").isEqualTo(contract.id)

        webTestClient.get()
            .uri("/query/accounts/${account.id}/future-transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(2)
            .jsonPath("$.items[0].description").isEqualTo("Future expense")
            .jsonPath("$.items[0].projectedBalanceAfter").isEqualTo(-300)
            .jsonPath("$.items[1].description").isEqualTo("Future transfer")
            .jsonPath("$.items[1].projectedBalanceAfter").isEqualTo(-300)

        webTestClient.get()
            .uri("/query/pockets/${contractPocket.id}/future-transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(2)
            .jsonPath("$.items[0].description").isEqualTo("Future expense")
            .jsonPath("$.items[0].projectedBalanceAfter").isEqualTo(-300)
            .jsonPath("$.items[1].description").isEqualTo("Future transfer")
            .jsonPath("$.items[1].projectedBalanceAfter").isEqualTo(-350)

        webTestClient.get()
            .uri("/query/contracts/${contract.id}/future-transactions?limit=10&fromDate=2026-04-21&toDate=2026-04-21")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].description").isEqualTo("Future transfer")
            .jsonPath("$.items[0].contractId").isEqualTo(contract.id)
    }

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
}
