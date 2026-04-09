package de.chennemann.plannr.server.query

import de.chennemann.plannr.server.accounts.usecases.CreateAccount
import de.chennemann.plannr.server.pockets.usecases.CreatePocket
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.transactions.usecases.ArchiveTransaction
import de.chennemann.plannr.server.transactions.usecases.CreateTransaction
import de.chennemann.plannr.server.transactions.usecases.UnarchiveTransaction
import de.chennemann.plannr.server.transactions.usecases.UpdateTransaction
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient

class TransactionProjectionIntegrationTest : ApiIntegrationTest() {
    @Autowired lateinit var databaseClient: DatabaseClient
    @Autowired lateinit var createAccount: CreateAccount
    @Autowired lateinit var createPocket: CreatePocket
    @Autowired lateinit var createTransaction: CreateTransaction
    @Autowired lateinit var updateTransaction: UpdateTransaction
    @Autowired lateinit var archiveTransaction: ArchiveTransaction
    @Autowired lateinit var unarchiveTransaction: UnarchiveTransaction

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
    fun `projects transactions into account and pocket query models including historical rewrites`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "same_day",
            ),
        )
        val pocket = createPocket(
            CreatePocket.Command(
                accountId = account.id,
                name = "Wallet",
                description = null,
                color = 123,
                isDefault = true,
            ),
        )

        val expense1 = createTransaction(
            CreateTransaction.Command(
                type = "expense",
                status = "booked",
                transactionDate = "2026-04-10",
                amount = 100,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Groceries",
                partnerId = null,
                sourcePocketId = pocket.id,
                destinationPocketId = null,
            ),
        )
        val income = createTransaction(
            CreateTransaction.Command(
                type = "income",
                status = "booked",
                transactionDate = "2026-04-15",
                amount = 200,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Salary",
                partnerId = null,
                sourcePocketId = null,
                destinationPocketId = pocket.id,
            ),
        )

        webTestClient.get()
            .uri("/query/accounts/${account.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(100)

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(2)
            .jsonPath("$.items[0].transactionId").isEqualTo(income.id)
            .jsonPath("$.items[0].historyPosition").isEqualTo(2)
            .jsonPath("$.items[0].signedAmount").isEqualTo(200)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(100)
            .jsonPath("$.items[1].transactionId").isEqualTo(expense1.id)
            .jsonPath("$.items[1].historyPosition").isEqualTo(1)
            .jsonPath("$.items[1].signedAmount").isEqualTo(-100)
            .jsonPath("$.items[1].balanceAfter").isEqualTo(-100)

        val expense2 = createTransaction(
            CreateTransaction.Command(
                type = "expense",
                status = "booked",
                transactionDate = "2026-04-12",
                amount = 50,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Coffee",
                partnerId = null,
                sourcePocketId = pocket.id,
                destinationPocketId = null,
            ),
        )

        webTestClient.get()
            .uri("/query/accounts/${account.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(50)

        webTestClient.get()
            .uri("/query/pockets/${pocket.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(50)

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(3)
            .jsonPath("$.items[0].transactionId").isEqualTo(income.id)
            .jsonPath("$.items[0].historyPosition").isEqualTo(3)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(50)
            .jsonPath("$.items[1].transactionId").isEqualTo(expense2.id)
            .jsonPath("$.items[1].historyPosition").isEqualTo(2)
            .jsonPath("$.items[1].balanceAfter").isEqualTo(-150)
            .jsonPath("$.items[2].transactionId").isEqualTo(expense1.id)
            .jsonPath("$.items[2].historyPosition").isEqualTo(1)
            .jsonPath("$.items[2].balanceAfter").isEqualTo(-100)

        updateTransaction(
            UpdateTransaction.Command(
                id = expense2.id,
                type = "expense",
                status = "booked",
                transactionDate = "2026-04-09",
                amount = 70,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Coffee",
                partnerId = null,
                sourcePocketId = pocket.id,
                destinationPocketId = null,
            ),
        )

        webTestClient.get()
            .uri("/query/accounts/${account.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(30)

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(3)
            .jsonPath("$.items[0].transactionId").isEqualTo(income.id)
            .jsonPath("$.items[0].historyPosition").isEqualTo(3)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(30)
            .jsonPath("$.items[1].transactionId").isEqualTo(expense1.id)
            .jsonPath("$.items[1].historyPosition").isEqualTo(2)
            .jsonPath("$.items[1].balanceAfter").isEqualTo(-170)
            .jsonPath("$.items[2].transactionId").isEqualTo(expense2.id)
            .jsonPath("$.items[2].historyPosition").isEqualTo(1)
            .jsonPath("$.items[2].balanceAfter").isEqualTo(-70)

        archiveTransaction(expense1.id)

        webTestClient.get()
            .uri("/query/accounts/${account.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(130)

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(2)
            .jsonPath("$.items[0].transactionId").isEqualTo(income.id)
            .jsonPath("$.items[0].historyPosition").isEqualTo(2)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(130)
            .jsonPath("$.items[1].transactionId").isEqualTo(expense2.id)
            .jsonPath("$.items[1].historyPosition").isEqualTo(1)
            .jsonPath("$.items[1].balanceAfter").isEqualTo(-70)

        unarchiveTransaction(expense1.id)

        webTestClient.get()
            .uri("/query/accounts/${account.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(30)

        webTestClient.get()
            .uri("/query/pockets/${pocket.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(3)
            .jsonPath("$.items[0].transactionId").isEqualTo(income.id)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(30)
            .jsonPath("$.items[1].transactionId").isEqualTo(expense1.id)
            .jsonPath("$.items[1].balanceAfter").isEqualTo(-170)
            .jsonPath("$.items[2].transactionId").isEqualTo(expense2.id)
            .jsonPath("$.items[2].balanceAfter").isEqualTo(-70)
    }

    @Test
    fun `same account transfer projects one account row and two pocket rows`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "same_day",
            ),
        )
        val sourcePocket = createPocket(CreatePocket.Command(account.id, "Checking", null, 100, true))
        val destinationPocket = createPocket(CreatePocket.Command(account.id, "Savings", null, 200, false))

        val transfer = createTransaction(
            CreateTransaction.Command(
                type = "transfer",
                status = "booked",
                transactionDate = "2026-04-20",
                amount = 40,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Move to savings",
                partnerId = null,
                sourcePocketId = sourcePocket.id,
                destinationPocketId = destinationPocket.id,
            ),
        )

        webTestClient.get()
            .uri("/query/accounts/${account.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(0)

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].transactionId").isEqualTo(transfer.id)
            .jsonPath("$.items[0].signedAmount").isEqualTo(0)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(0)
            .jsonPath("$.items[0].sourcePocketName").isEqualTo("Checking")
            .jsonPath("$.items[0].destinationPocketName").isEqualTo("Savings")

        webTestClient.get()
            .uri("/query/pockets/${sourcePocket.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(-40)

        webTestClient.get()
            .uri("/query/pockets/${destinationPocket.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(40)

        webTestClient.get()
            .uri("/query/pockets/${sourcePocket.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].transactionId").isEqualTo(transfer.id)
            .jsonPath("$.items[0].signedAmount").isEqualTo(-40)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(-40)
            .jsonPath("$.items[0].transferPocketName").isEqualTo("Savings")

        webTestClient.get()
            .uri("/query/pockets/${destinationPocket.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].transactionId").isEqualTo(transfer.id)
            .jsonPath("$.items[0].signedAmount").isEqualTo(40)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(40)
            .jsonPath("$.items[0].transferPocketName").isEqualTo("Checking")
    }

    private fun insertCurrency(code: String) {
        runBlocking {
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
}
