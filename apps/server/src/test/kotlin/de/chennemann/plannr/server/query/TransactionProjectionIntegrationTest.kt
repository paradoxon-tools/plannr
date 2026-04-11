package de.chennemann.plannr.server.query

import de.chennemann.plannr.server.accounts.usecases.CreateAccount
import de.chennemann.plannr.server.accounts.usecases.UpdateAccount
import de.chennemann.plannr.server.pockets.usecases.CreatePocket
import de.chennemann.plannr.server.query.projection.TransactionQueryProjectionService
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.transactions.usecases.ArchiveTransaction
import de.chennemann.plannr.server.transactions.usecases.CreateTransaction
import de.chennemann.plannr.server.transactions.usecases.UnarchiveTransaction
import de.chennemann.plannr.server.transactions.usecases.UpdateTransaction
import java.time.LocalDate
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import kotlin.test.assertEquals

class TransactionProjectionIntegrationTest : ApiIntegrationTest() {
    @Autowired lateinit var databaseClient: DatabaseClient
    @Autowired lateinit var createAccount: CreateAccount
    @Autowired lateinit var updateAccount: UpdateAccount
    @Autowired lateinit var createPocket: CreatePocket
    @Autowired lateinit var createTransaction: CreateTransaction
    @Autowired lateinit var updateTransaction: UpdateTransaction
    @Autowired lateinit var archiveTransaction: ArchiveTransaction
    @Autowired lateinit var unarchiveTransaction: UnarchiveTransaction
    @Autowired lateinit var transactionQueryProjectionService: TransactionQueryProjectionService

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
    fun `future transactions do not alter current balances or historical feeds`() = runBlocking {
        val today = LocalDate.now()
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val pocket = createPocket(CreatePocket.Command(account.id, "Wallet", null, 123, true))

        createTransaction(
            CreateTransaction.Command(
                type = "EXPENSE",
                status = "CLEARED",
                transactionDate = today.minusDays(1).toString(),
                amount = 40,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Yesterday",
                partnerId = null,
                sourcePocketId = pocket.id,
                destinationPocketId = null,
            ),
        )
        createTransaction(
            CreateTransaction.Command(
                type = "EXPENSE",
                status = "PENDING",
                transactionDate = today.plusDays(1).toString(),
                amount = 60,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Tomorrow",
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
            .jsonPath("$.currentBalance").isEqualTo(-40)

        webTestClient.get()
            .uri("/query/pockets/${pocket.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(-40)

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].description").isEqualTo("Yesterday")
    }

    @Test
    fun `projects transactions into account and pocket query models including historical rewrites`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
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
                type = "EXPENSE",
                status = "CLEARED",
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
                type = "INCOME",
                status = "CLEARED",
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
                type = "EXPENSE",
                status = "CLEARED",
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
                type = "EXPENSE",
                status = "CLEARED",
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
    fun `same date transactions keep deterministic history order`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val pocket = createPocket(CreatePocket.Command(account.id, "Wallet", null, 123, true))

        val first = createTransaction(
            CreateTransaction.Command(
                type = "EXPENSE",
                status = "CLEARED",
                transactionDate = "2026-04-10",
                amount = 10,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "First same day",
                partnerId = null,
                sourcePocketId = pocket.id,
                destinationPocketId = null,
            ),
        )
        val second = createTransaction(
            CreateTransaction.Command(
                type = "EXPENSE",
                status = "CLEARED",
                transactionDate = "2026-04-10",
                amount = 20,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Second same day",
                partnerId = null,
                sourcePocketId = pocket.id,
                destinationPocketId = null,
            ),
        )

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(2)
            .jsonPath("$.items[0].transactionId").isEqualTo(second.id)
            .jsonPath("$.items[0].historyPosition").isEqualTo(2)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(-30)
            .jsonPath("$.items[1].transactionId").isEqualTo(first.id)
            .jsonPath("$.items[1].historyPosition").isEqualTo(1)
            .jsonPath("$.items[1].balanceAfter").isEqualTo(-10)

        webTestClient.get()
            .uri("/query/pockets/${pocket.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items[0].transactionId").isEqualTo(second.id)
            .jsonPath("$.items[1].transactionId").isEqualTo(first.id)
    }

    @Test
    fun `same account transfer projects one account row and two pocket rows`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val sourcePocket = createPocket(CreatePocket.Command(account.id, "Checking", null, 100, true))
        val destinationPocket = createPocket(CreatePocket.Command(account.id, "Savings", null, 200, false))

        val transfer = createTransaction(
            CreateTransaction.Command(
                type = "TRANSFER",
                status = "CLEARED",
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
            .jsonPath("$.items[0].sourcePocketId").isEqualTo(sourcePocket.id)
            .jsonPath("$.items[0].sourcePocketName").isEqualTo("Checking")
            .jsonPath("$.items[0].sourcePocketColor").isEqualTo(100)
            .jsonPath("$.items[0].destinationPocketId").isEqualTo(destinationPocket.id)
            .jsonPath("$.items[0].destinationPocketName").isEqualTo("Savings")
            .jsonPath("$.items[0].destinationPocketColor").isEqualTo(200)

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
            .jsonPath("$.items[0].transferPocketId").isEqualTo(destinationPocket.id)
            .jsonPath("$.items[0].transferPocketName").isEqualTo("Savings")
            .jsonPath("$.items[0].transferPocketColor").isEqualTo(200)

        webTestClient.get()
            .uri("/query/pockets/${destinationPocket.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].transactionId").isEqualTo(transfer.id)
            .jsonPath("$.items[0].signedAmount").isEqualTo(40)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(40)
            .jsonPath("$.items[0].transferPocketId").isEqualTo(sourcePocket.id)
            .jsonPath("$.items[0].transferPocketName").isEqualTo("Checking")
            .jsonPath("$.items[0].transferPocketColor").isEqualTo(100)
    }

    @Test
    fun `repeated updates of the same historical transaction remain consistent`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val pocket = createPocket(CreatePocket.Command(account.id, "Wallet", null, 123, true))

        val tx1 = createTransaction(CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-10", 10, "EUR", null, null, "t1", null, pocket.id, null))
        val tx2 = createTransaction(CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-12", 20, "EUR", null, null, "t2", null, pocket.id, null))

        updateTransaction(UpdateTransaction.Command(tx1.id, "EXPENSE", "CLEARED", "2026-04-11", 15, "EUR", null, null, "t1", null, pocket.id, null))
        updateTransaction(UpdateTransaction.Command(tx1.id, "EXPENSE", "CLEARED", "2026-04-13", 25, "EUR", null, null, "t1", null, pocket.id, null))

        webTestClient.get()
            .uri("/query/accounts/${account.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(-45)

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(2)
            .jsonPath("$.items[0].transactionId").isEqualTo(tx1.id)
            .jsonPath("$.items[0].historyPosition").isEqualTo(2)
            .jsonPath("$.items[0].signedAmount").isEqualTo(-25)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(-45)
            .jsonPath("$.items[1].transactionId").isEqualTo(tx2.id)
            .jsonPath("$.items[1].historyPosition").isEqualTo(1)
            .jsonPath("$.items[1].signedAmount").isEqualTo(-20)
            .jsonPath("$.items[1].balanceAfter").isEqualTo(-20)
    }

    @Test
    fun `updating expense to transfer rewrites affected account and pocket histories`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val sourcePocket = createPocket(CreatePocket.Command(account.id, "Checking", null, 100, true))
        val destinationPocket = createPocket(CreatePocket.Command(account.id, "Savings", null, 200, false))

        val transaction = createTransaction(
            CreateTransaction.Command(
                type = "EXPENSE",
                status = "CLEARED",
                transactionDate = "2026-04-20",
                amount = 50,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Originally expense",
                partnerId = null,
                sourcePocketId = sourcePocket.id,
                destinationPocketId = null,
            ),
        )

        updateTransaction(
            UpdateTransaction.Command(
                id = transaction.id,
                type = "TRANSFER",
                status = "CLEARED",
                transactionDate = "2026-04-20",
                amount = 50,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Now transfer",
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
            .jsonPath("$.items[0].transactionId").isEqualTo(transaction.id)
            .jsonPath("$.items[0].type").isEqualTo("TRANSFER")
            .jsonPath("$.items[0].signedAmount").isEqualTo(0)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(0)
            .jsonPath("$.items[0].sourcePocketName").isEqualTo("Checking")
            .jsonPath("$.items[0].destinationPocketName").isEqualTo("Savings")

        webTestClient.get()
            .uri("/query/pockets/${sourcePocket.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(-50)

        webTestClient.get()
            .uri("/query/pockets/${destinationPocket.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(50)

        webTestClient.get()
            .uri("/query/pockets/${destinationPocket.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].transactionId").isEqualTo(transaction.id)
            .jsonPath("$.items[0].signedAmount").isEqualTo(50)
            .jsonPath("$.items[0].transferPocketName").isEqualTo("Checking")
    }

    @Test
    fun `updating transaction source pocket moves it between pocket histories`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val oldPocket = createPocket(CreatePocket.Command(account.id, "Old pocket", null, 100, true))
        val newPocket = createPocket(CreatePocket.Command(account.id, "New pocket", null, 200, false))

        val transaction = createTransaction(
            CreateTransaction.Command(
                type = "EXPENSE",
                status = "CLEARED",
                transactionDate = "2026-04-20",
                amount = 80,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Pocket move",
                partnerId = null,
                sourcePocketId = oldPocket.id,
                destinationPocketId = null,
            ),
        )

        updateTransaction(
            UpdateTransaction.Command(
                id = transaction.id,
                type = "EXPENSE",
                status = "CLEARED",
                transactionDate = "2026-04-20",
                amount = 80,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Pocket move",
                partnerId = null,
                sourcePocketId = newPocket.id,
                destinationPocketId = null,
            ),
        )

        webTestClient.get()
            .uri("/query/pockets/${oldPocket.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(0)

        webTestClient.get()
            .uri("/query/pockets/${oldPocket.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(0)

        webTestClient.get()
            .uri("/query/pockets/${newPocket.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(-80)

        webTestClient.get()
            .uri("/query/pockets/${newPocket.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].transactionId").isEqualTo(transaction.id)
            .jsonPath("$.items[0].signedAmount").isEqualTo(-80)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(-80)

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items[0].sourcePocketName").isEqualTo("New pocket")
    }

    @Test
    fun `pagination remains valid after repeated historical rewrites`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val pocket = createPocket(CreatePocket.Command(account.id, "Wallet", null, 123, true))

        val tx1 = createTransaction(CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-10", 10, "EUR", null, null, "t1", null, pocket.id, null))
        val tx2 = createTransaction(CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-12", 20, "EUR", null, null, "t2", null, pocket.id, null))
        val tx3 = createTransaction(CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-14", 30, "EUR", null, null, "t3", null, pocket.id, null))

        updateTransaction(UpdateTransaction.Command(tx1.id, "EXPENSE", "CLEARED", "2026-04-13", 10, "EUR", null, null, "t1", null, pocket.id, null))
        archiveTransaction(tx2.id)
        val tx4 = createTransaction(CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-11", 15, "EUR", null, null, "t4", null, pocket.id, null))

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=1")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].transactionId").isEqualTo(tx3.id)
            .jsonPath("$.nextBefore").isEqualTo(3)

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=1&before=3")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].transactionId").isEqualTo(tx1.id)
            .jsonPath("$.nextBefore").isEqualTo(2)

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=1&before=2")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].transactionId").isEqualTo(tx4.id)
            .jsonPath("$.nextBefore").isEqualTo(1)
    }

    @Test
    fun `transfer destination amount is used for destination pocket while account stays neutral`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val sourcePocket = createPocket(CreatePocket.Command(account.id, "Checking", null, 100, true))
        val destinationPocket = createPocket(CreatePocket.Command(account.id, "Savings", null, 200, false))

        val transfer = createTransaction(
            CreateTransaction.Command(
                type = "TRANSFER",
                status = "CLEARED",
                transactionDate = "2026-04-21",
                amount = 100,
                currencyCode = "EUR",
                exchangeRate = "1.3",
                destinationAmount = 130,
                description = "Exchange transfer",
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
            .jsonPath("$.items[0].transactionId").isEqualTo(transfer.id)
            .jsonPath("$.items[0].signedAmount").isEqualTo(0)
            .jsonPath("$.items[0].transactionAmount").isEqualTo(100)

        webTestClient.get()
            .uri("/query/pockets/${sourcePocket.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items[0].signedAmount").isEqualTo(-100)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(-100)

        webTestClient.get()
            .uri("/query/pockets/${destinationPocket.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items[0].signedAmount").isEqualTo(130)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(130)

        webTestClient.get()
            .uri("/query/pockets/${destinationPocket.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(130)
    }

    @Test
    fun `income destination row uses positive signed amount in pocket feed`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val pocket = createPocket(CreatePocket.Command(account.id, "Wallet", null, 123, true))

        val income = createTransaction(
            CreateTransaction.Command(
                type = "INCOME",
                status = "CLEARED",
                transactionDate = "2026-04-22",
                amount = 75,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Income",
                partnerId = null,
                sourcePocketId = null,
                destinationPocketId = pocket.id,
            ),
        )

        webTestClient.get()
            .uri("/query/pockets/${pocket.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].transactionId").isEqualTo(income.id)
            .jsonPath("$.items[0].signedAmount").isEqualTo(75)
            .jsonPath("$.items[0].balanceAfter").isEqualTo(75)
    }

    @Test
    fun `account metadata changes do not break feed readability or association`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val pocket = createPocket(CreatePocket.Command(account.id, "Wallet", null, 123, true))
        val transaction = createTransaction(
            CreateTransaction.Command(
                type = "EXPENSE",
                status = "CLEARED",
                transactionDate = "2026-04-22",
                amount = 33,
                currencyCode = "EUR",
                exchangeRate = null,
                destinationAmount = null,
                description = "Expense",
                partnerId = null,
                sourcePocketId = pocket.id,
                destinationPocketId = null,
            ),
        )

        val updatedAccount = UpdateAccount.Command(
            id = account.id,
            name = "Renamed account",
            institution = "New bank",
            currencyCode = "EUR",
            weekendHandling = "MOVE_AFTER",
        )
        updateAccount(updatedAccount)

        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(1)
            .jsonPath("$.items[0].accountId").isEqualTo(account.id)
            .jsonPath("$.items[0].transactionId").isEqualTo(transaction.id)
            .jsonPath("$.items[0].sourcePocketName").isEqualTo("Wallet")
    }

    @Test
    fun `history positions remain unique after repeated rewrites`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val pocket = createPocket(CreatePocket.Command(account.id, "Wallet", null, 123, true))

        val tx1 = createTransaction(CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-10", 10, "EUR", null, null, "t1", null, pocket.id, null))
        val tx2 = createTransaction(CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-12", 20, "EUR", null, null, "t2", null, pocket.id, null))
        val tx3 = createTransaction(CreateTransaction.Command("INCOME", "CLEARED", "2026-04-15", 40, "EUR", null, null, "t3", null, null, pocket.id))

        updateTransaction(UpdateTransaction.Command(tx1.id, "EXPENSE", "CLEARED", "2026-04-14", 15, "EUR", null, null, "t1", null, pocket.id, null))
        archiveTransaction(tx2.id)
        unarchiveTransaction(tx2.id)
        updateTransaction(UpdateTransaction.Command(tx2.id, "EXPENSE", "CLEARED", "2026-04-11", 25, "EUR", null, null, "t2", null, pocket.id, null))

        assertEquals(
            singleLong("SELECT COUNT(*) AS value FROM account_transaction_feed WHERE account_id = '${account.id}'"),
            singleLong("SELECT COUNT(DISTINCT history_position) AS value FROM account_transaction_feed WHERE account_id = '${account.id}'"),
        )
        assertEquals(
            singleLong("SELECT COUNT(*) AS value FROM pocket_transaction_feed WHERE pocket_id = '${pocket.id}'"),
            singleLong("SELECT COUNT(DISTINCT history_position) AS value FROM pocket_transaction_feed WHERE pocket_id = '${pocket.id}'"),
        )
        webTestClient.get()
            .uri("/query/accounts/${account.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(0)
        webTestClient.get()
            .uri("/query/pockets/${pocket.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentBalance").isEqualTo(0)
        webTestClient.get()
            .uri("/query/accounts/${account.id}/transactions?limit=10")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.items.length()").isEqualTo(3)
    }

    @Test
    fun `rebuilding the same projection state is deterministic`() = runBlocking {
        val account = createAccount(
            CreateAccount.Command(
                name = "Main account",
                institution = "Demo Bank",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
            ),
        )
        val pocket = createPocket(CreatePocket.Command(account.id, "Wallet", null, 123, true))
        val tx1 = createTransaction(CreateTransaction.Command("EXPENSE", "CLEARED", "2026-04-10", 10, "EUR", null, null, "t1", null, pocket.id, null))
        val tx2 = createTransaction(CreateTransaction.Command("INCOME", "CLEARED", "2026-04-12", 30, "EUR", null, null, "t2", null, null, pocket.id))

        transactionQueryProjectionService.rebuildFor(after = tx1)
        transactionQueryProjectionService.rebuildFor(after = tx2)
        val firstAccountRows = queryRows("SELECT transaction_id, history_position, signed_amount, balance_after FROM account_transaction_feed WHERE account_id = '${account.id}' ORDER BY history_position")
        val firstPocketRows = queryRows("SELECT transaction_id, history_position, signed_amount, balance_after FROM pocket_transaction_feed WHERE pocket_id = '${pocket.id}' ORDER BY history_position")
        val firstAccountBalance = singleLong("SELECT current_balance AS value FROM account_query WHERE account_id = '${account.id}'")
        val firstPocketBalance = singleLong("SELECT current_balance AS value FROM pocket_query WHERE pocket_id = '${pocket.id}'")

        transactionQueryProjectionService.rebuildFor(after = tx1)
        transactionQueryProjectionService.rebuildFor(after = tx2)
        val secondAccountRows = queryRows("SELECT transaction_id, history_position, signed_amount, balance_after FROM account_transaction_feed WHERE account_id = '${account.id}' ORDER BY history_position")
        val secondPocketRows = queryRows("SELECT transaction_id, history_position, signed_amount, balance_after FROM pocket_transaction_feed WHERE pocket_id = '${pocket.id}' ORDER BY history_position")
        val secondAccountBalance = singleLong("SELECT current_balance AS value FROM account_query WHERE account_id = '${account.id}'")
        val secondPocketBalance = singleLong("SELECT current_balance AS value FROM pocket_query WHERE pocket_id = '${pocket.id}'")

        assertEquals(firstAccountRows, secondAccountRows)
        assertEquals(firstPocketRows, secondPocketRows)
        assertEquals(firstAccountBalance, secondAccountBalance)
        assertEquals(firstPocketBalance, secondPocketBalance)
    }

    private suspend fun singleLong(sql: String): Long =
        (databaseClient.sql(sql).fetch().one().awaitSingle().getValue("value") as Number).toLong()

    private suspend fun queryRows(sql: String): List<Map<String, Any?>> =
        databaseClient.sql(sql)
            .fetch()
            .all()
            .map { row -> row.mapValues { it.value } }
            .collectList()
            .awaitSingle()

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
