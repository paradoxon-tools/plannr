package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.query.projection.InMemoryProjectionDirtyScopeRepository
import de.chennemann.plannr.server.query.projection.ProjectionDirtyScopeService
import de.chennemann.plannr.server.recurringtransactions.support.InMemoryRecurringTransactionRepository
import de.chennemann.plannr.server.recurringtransactions.support.RecurringTransactionFixtures
import de.chennemann.plannr.server.transactions.support.InMemoryTransactionRepository
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class RecurringTransactionMaterializerTest {
    @Test
    fun `materializes recurring transactions as pending canonical rows`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(RecurringTransactionFixtures.recurringTransaction(firstOccurrenceDate = "2024-04-10", finalOccurrenceDate = "2024-04-12", recurrenceType = "DAILY", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null))
        }
        val transactionRepository = InMemoryTransactionRepository()
        val materializer = materializer(recurringRepository, transactionRepository)

        val created = materializer.materializeAll()
        val rows = transactionRepository.all()

        assertEquals(3, created.createdCount)
        assertEquals(3, rows.size)
        assertEquals(listOf("PENDING"), rows.map { it.status }.distinct())
        assertEquals(listOf("RECURRING_MATERIALIZED"), rows.map { it.transactionOrigin }.distinct())
        assertEquals(listOf("Monthly internet"), rows.map { it.description }.distinct())
        assertEquals(listOf("2024-04-10", "2024-04-11", "2024-04-12"), rows.map { it.transactionDate })
    }

    @Test
    fun `reruns are idempotent and advance last materialized date only when creating rows`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(RecurringTransactionFixtures.recurringTransaction(firstOccurrenceDate = "2024-04-10", finalOccurrenceDate = "2024-04-11", recurrenceType = "DAILY", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null))
        }
        val transactionRepository = InMemoryTransactionRepository()
        val materializer = materializer(recurringRepository, transactionRepository)

        assertEquals(2, materializer.materializeAll().createdCount)
        assertEquals(0, materializer.materializeAll().createdCount)
        assertEquals(2, transactionRepository.all().size)
        assertEquals("2024-04-11", recurringRepository.findById(RecurringTransactionFixtures.DEFAULT_ID)?.lastMaterializedDate)
    }

    @Test
    fun `weekend handling shifts materialized dates`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(RecurringTransactionFixtures.recurringTransaction(firstOccurrenceDate = "2024-04-13", finalOccurrenceDate = "2024-04-13", recurrenceType = "NONE", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null))
        }
        val transactionRepository = InMemoryTransactionRepository()
        val accountRepository = InMemoryAccountRepository().apply {
            save(AccountFixtures.account(weekendHandling = "MOVE_AFTER"))
        }
        val materializer = materializer(recurringRepository, transactionRepository, accountRepository)

        materializer.materializeAll()

        assertEquals(listOf("2024-04-15"), transactionRepository.all().map { it.transactionDate })
    }

    @Test
    fun `horizon covers end of next month and at least five future sparse occurrences`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(RecurringTransactionFixtures.recurringTransaction(firstOccurrenceDate = "2024-04-15", finalOccurrenceDate = null, recurrenceType = "YEARLY", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = listOf(15), monthsOfYear = listOf(4)))
        }
        val transactionRepository = InMemoryTransactionRepository()
        val materializer = materializer(recurringRepository, transactionRepository, today = LocalDate.parse("2024-04-10"))

        materializer.materializeAll()

        assertEquals(
            listOf("2024-04-15", "2025-04-15", "2026-04-15", "2027-04-15", "2028-04-15"),
            transactionRepository.all().map { it.transactionDate }.take(5),
        )
    }

    @Test
    fun `existing modified chains block duplicate rematerialization`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(RecurringTransactionFixtures.recurringTransaction(firstOccurrenceDate = "2024-04-10", finalOccurrenceDate = "2024-04-10", recurrenceType = "NONE", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null))
        }
        val transactionRepository = InMemoryTransactionRepository().apply {
            val original = save(
                de.chennemann.plannr.server.transactions.domain.TransactionRecord(
                    id = "txn_original",
                    accountId = "acc_123",
                    type = "EXPENSE",
                    status = "PENDING",
                    transactionDate = "2024-04-10",
                    amount = 4999,
                    currencyCode = "EUR",
                    exchangeRate = null,
                    destinationAmount = null,
                    description = "Monthly internet",
                    partnerId = null,
                    pocketId = "poc_123",
                    sourcePocketId = "poc_123",
                    destinationPocketId = null,
                    parentTransactionId = null,
                    recurringTransactionId = "rtx_123",
                    modifiedById = null,
                    transactionOrigin = "RECURRING_MATERIALIZED",
                    isArchived = false,
                    createdAt = 1L,
                ),
            )
            val child = save(
                de.chennemann.plannr.server.transactions.domain.TransactionRecord(
                    id = "txn_child",
                    accountId = "acc_123",
                    type = "EXPENSE",
                    status = "PENDING",
                    transactionDate = "2024-04-10",
                    amount = 4999,
                    currencyCode = "EUR",
                    exchangeRate = null,
                    destinationAmount = null,
                    description = "Monthly internet",
                    partnerId = null,
                    pocketId = "poc_123",
                    sourcePocketId = "poc_123",
                    destinationPocketId = null,
                    parentTransactionId = original.id,
                    recurringTransactionId = "rtx_123",
                    modifiedById = null,
                    transactionOrigin = "RECURRING_MODIFICATION",
                    isArchived = false,
                    createdAt = 1L,
                ),
            )
            update(
                de.chennemann.plannr.server.transactions.domain.TransactionRecord(
                    id = original.id,
                    accountId = "acc_123",
                    type = "EXPENSE",
                    status = "PENDING",
                    transactionDate = "2024-04-10",
                    amount = 4999,
                    currencyCode = "EUR",
                    exchangeRate = null,
                    destinationAmount = null,
                    description = "Monthly internet",
                    partnerId = null,
                    pocketId = "poc_123",
                    sourcePocketId = "poc_123",
                    destinationPocketId = null,
                    parentTransactionId = null,
                    recurringTransactionId = "rtx_123",
                    modifiedById = child.id,
                    transactionOrigin = "RECURRING_MATERIALIZED",
                    isArchived = false,
                    createdAt = 1L,
                ),
            )
        }
        val materializer = materializer(recurringRepository, transactionRepository)

        assertEquals(0, materializer.materializeAll().createdCount)
        assertEquals(2, transactionRepository.all().size)
    }

    @Test
    fun `archived recurring templates are skipped`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(RecurringTransactionFixtures.recurringTransaction(isArchived = true))
        }
        val transactionRepository = InMemoryTransactionRepository()
        val materializer = materializer(recurringRepository, transactionRepository)

        assertEquals(0, materializer.materializeAll().createdCount)
        assertEquals(emptyList(), transactionRepository.all())
    }

    private fun materializer(
        recurringRepository: InMemoryRecurringTransactionRepository,
        transactionRepository: InMemoryTransactionRepository,
        accountRepository: InMemoryAccountRepository = InMemoryAccountRepository().apply { save(AccountFixtures.account(weekendHandling = "NO_SHIFT")) },
        today: LocalDate = LocalDate.parse("2024-04-10"),
    ): RecurringTransactionMaterializer = RecurringTransactionMaterializer(
            recurringTransactionRepository = recurringRepository,
            transactionRepository = transactionRepository,
            accountRepository = accountRepository,
            transactionIdGenerator = { "txn_${transactionRepository.all().size + 1}" },
            localDateProvider = { today },
            timeProvider = { 1L },
            dirtyScopeService = ProjectionDirtyScopeService(InMemoryProjectionDirtyScopeRepository(), timeProvider = { 1L }),
        )
}
