package de.chennemann.plannr.server.transactions.persistence

import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import de.chennemann.plannr.server.currencies.support.CurrencyFixtures
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.recurringtransactions.support.RecurringTransactionFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class R2dbcTransactionRepositoryTest : ApiIntegrationTest() {
    @Autowired lateinit var transactionRepository: TransactionRepository
    @Autowired lateinit var currencyRepository: CurrencyRepository
    @Autowired lateinit var accountRepository: AccountRepository
    @Autowired lateinit var pocketRepository: PocketRepository
    @Autowired lateinit var recurringTransactionRepository: RecurringTransactionRepository

    @BeforeEach
    fun setUp() {
        runBlocking {
            cleanDatabase("transactions", "recurring_transactions", "pocket_transaction_feed", "account_transaction_feed", "pocket_query", "account_query", "pockets", "accounts", "currencies")
            currencyRepository.save(CurrencyFixtures.currency())
            accountRepository.save(AccountFixtures.account())
            pocketRepository.save(PocketFixtures.pocket())
            pocketRepository.save(PocketFixtures.pocket(id = "poc_456", name = "Savings"))
            recurringTransactionRepository.save(
                RecurringTransactionFixtures.recurringTransaction(
                    id = "rtx_123",
                    contractId = null,
                    accountId = "acc_123",
                    sourcePocketId = "poc_123",
                    destinationPocketId = null,
                    partnerId = null,
                    transactionType = "EXPENSE",
                    recurrenceType = "NONE",
                    firstOccurrenceDate = "2026-04-10",
                    finalOccurrenceDate = "2026-04-10",
                    daysOfWeek = null,
                    weeksOfMonth = null,
                    daysOfMonth = null,
                    monthsOfYear = null,
                ),
            )
        }
    }

    @Test
    fun `round trips canonical enums through persistence`() = runBlocking {
        val saved = transactionRepository.save(transaction())

        val found = transactionRepository.findById(saved.id)

        assertEquals("EXPENSE", found?.type)
        assertEquals("CLEARED", found?.status)
        assertEquals("EUR", found?.currencyCode)
        assertEquals("MANUAL", found?.transactionOrigin)
        assertEquals("poc_123", found?.pocketId)
        assertEquals("poc_123", found?.sourcePocketId)
    }

    @Test
    fun `visible queries exclude archived and hidden originals but include modifications`() = runBlocking {
        val original = transactionRepository.save(transaction(id = "txn_original", transactionDate = "2026-04-10"))
        val modification = transactionRepository.save(
            transaction(
                id = "txn_modification",
                transactionDate = "2026-04-10",
                amount = 120,
                parentTransactionId = original.id,
                recurringTransactionId = "rtx_123",
            ),
        )
        transactionRepository.update(
            transaction(
                id = original.id,
                transactionDate = original.transactionDate,
                amount = original.amount,
                parentTransactionId = original.parentTransactionId,
                recurringTransactionId = original.recurringTransactionId,
                modifiedById = modification.id,
            ),
        )
        transactionRepository.save(transaction(id = "txn_archived", transactionDate = "2026-04-11", isArchived = true))

        assertEquals(listOf(modification.id), transactionRepository.findVisibleByAccountId("acc_123").map { it.id })
        assertEquals(listOf(modification.id), transactionRepository.findVisibleByPocketId("poc_123").map { it.id })
        assertEquals(listOf(modification.id), transactionRepository.findVisibleByRecurringTransactionId("rtx_123").map { it.id })
    }

    @Test
    fun `duplicate root recurring occurrences are rejected by the database`() = runBlocking {
        transactionRepository.save(transaction(id = "txn_first", transactionDate = "2026-04-12", recurringTransactionId = "rtx_123"))

        assertFailsWith<Exception> {
            transactionRepository.save(transaction(id = "txn_duplicate", transactionDate = "2026-04-12", recurringTransactionId = "rtx_123"))
        }

        transactionRepository.save(
            transaction(
                id = "txn_child_same_date",
                transactionDate = "2026-04-12",
                parentTransactionId = "txn_first",
                recurringTransactionId = "rtx_123",
            ),
        )
    }

    @Test
    fun `visible unmodified recurring roots remain queryable`() = runBlocking {
        transactionRepository.save(transaction(id = "txn_root", transactionDate = "2026-04-12", status = "PENDING", recurringTransactionId = "rtx_123", transactionOrigin = "RECURRING_MATERIALIZED"))

        assertEquals(listOf("txn_root"), transactionRepository.findVisibleByRecurringTransactionId("rtx_123").map { it.id })
    }

    @Test
    fun `visible pending and future queries use visibility aware filtering`() = runBlocking {
        transactionRepository.save(transaction(id = "txn_pending_future", status = "PENDING", transactionDate = "2026-04-12"))
        transactionRepository.save(transaction(id = "txn_cleared_future", status = "CLEARED", transactionDate = "2026-04-13"))
        transactionRepository.save(transaction(id = "txn_mod", status = "CLEARED", transactionDate = "2026-05-01", isArchived = true))
        transactionRepository.save(transaction(id = "txn_hidden_future", status = "PENDING", transactionDate = "2026-04-14", modifiedById = "txn_mod"))
        transactionRepository.save(transaction(id = "txn_archived_future", status = "PENDING", transactionDate = "2026-04-15", isArchived = true))

        assertEquals(listOf("txn_pending_future"), transactionRepository.findVisiblePending().map { it.id })
        assertEquals(
            listOf("txn_pending_future", "txn_cleared_future"),
            transactionRepository.findVisibleFutureByAccountId("acc_123", "2026-04-12", "2026-04-13").map { it.id },
        )
        assertEquals(
            listOf("txn_pending_future", "txn_cleared_future"),
            transactionRepository.findVisibleFutureByPocketId("poc_123", "2026-04-12", "2026-04-13").map { it.id },
        )
    }

    private fun transaction(
        id: String = "txn_123",
        transactionDate: String = "2026-04-10",
        amount: Long = 100,
        parentTransactionId: String? = null,
        recurringTransactionId: String? = null,
        modifiedById: String? = null,
        status: String = "CLEARED",
        isArchived: Boolean = false,
        transactionOrigin: String = "MANUAL",
    ): TransactionRecord = TransactionRecord(
        id = id,
        accountId = "acc_123",
        type = "EXPENSE",
        status = status,
        transactionDate = transactionDate,
        amount = amount,
        currencyCode = "EUR",
        exchangeRate = null,
        destinationAmount = null,
        description = "desc",
        partnerId = null,
        pocketId = "poc_123",
        sourcePocketId = "poc_123",
        destinationPocketId = null,
        parentTransactionId = parentTransactionId,
        recurringTransactionId = recurringTransactionId,
        modifiedById = modifiedById,
        transactionOrigin = transactionOrigin,
        isArchived = isArchived,
        createdAt = 1L,
    )
}
