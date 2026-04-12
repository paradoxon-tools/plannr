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
    fun `materializes income and transfer rows while manual statuses remain unchanged`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(RecurringTransactionFixtures.recurringTransaction(id = "income", transactionType = "INCOME", sourcePocketId = null, destinationPocketId = "poc_123", recurrenceType = "NONE", firstOccurrenceDate = "2024-04-10", finalOccurrenceDate = "2024-04-10", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null))
            save(RecurringTransactionFixtures.recurringTransaction(id = "transfer", transactionType = "TRANSFER", sourcePocketId = "poc_123", destinationPocketId = "poc_456", recurrenceType = "NONE", firstOccurrenceDate = "2024-04-10", finalOccurrenceDate = "2024-04-10", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null))
        }
        val transactionRepository = InMemoryTransactionRepository().apply {
            save(
                de.chennemann.plannr.server.transactions.domain.TransactionRecord(
                    id = "manual",
                    accountId = "acc_123",
                    type = "EXPENSE",
                    status = "CLEARED",
                    transactionDate = "2024-04-09",
                    amount = 100,
                    currencyCode = "EUR",
                    exchangeRate = null,
                    destinationAmount = null,
                    description = "Manual",
                    partnerId = null,
                    pocketId = "poc_123",
                    sourcePocketId = "poc_123",
                    destinationPocketId = null,
                    parentTransactionId = null,
                    recurringTransactionId = null,
                    modifiedById = null,
                    transactionOrigin = "MANUAL",
                    isArchived = false,
                    createdAt = 1L,
                ),
            )
        }
        val materializer = materializer(recurringRepository, transactionRepository)

        materializer.materializeAll()

        val income = transactionRepository.all().first { it.recurringTransactionId == "income" }
        val transfer = transactionRepository.all().first { it.recurringTransactionId == "transfer" }
        val manual = transactionRepository.all().first { it.id == "manual" }
        assertEquals("PENDING", income.status)
        assertEquals("INCOME", income.type)
        assertEquals("poc_123", income.destinationPocketId)
        assertEquals("PENDING", transfer.status)
        assertEquals("TRANSFER", transfer.type)
        assertEquals("poc_123", transfer.sourcePocketId)
        assertEquals("poc_456", transfer.destinationPocketId)
        assertEquals("CLEARED", manual.status)
    }

    @Test
    fun `dense recurrences cover the full next calendar month and one off recurrence materializes once`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(RecurringTransactionFixtures.recurringTransaction(id = "dense", firstOccurrenceDate = "2024-04-10", finalOccurrenceDate = null, recurrenceType = "DAILY", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null))
            save(RecurringTransactionFixtures.recurringTransaction(id = "monthly_dense", firstOccurrenceDate = "2024-04-15", finalOccurrenceDate = null, recurrenceType = "MONTHLY", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = listOf(15), monthsOfYear = null))
            save(RecurringTransactionFixtures.recurringTransaction(id = "one_off", firstOccurrenceDate = "2024-04-11", finalOccurrenceDate = "2024-04-11", recurrenceType = "NONE", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null))
        }
        val transactionRepository = InMemoryTransactionRepository()
        val materializer = materializer(recurringRepository, transactionRepository, today = LocalDate.parse("2024-04-10"))

        materializer.materializeAll()
        materializer.materializeAll()

        val denseDates = transactionRepository.all().filter { it.recurringTransactionId == "dense" }.map { it.transactionDate }
        val monthlyDenseDates = transactionRepository.all().filter { it.recurringTransactionId == "monthly_dense" }.map { it.transactionDate }
        val oneOffDates = transactionRepository.all().filter { it.recurringTransactionId == "one_off" }.map { it.transactionDate }
        assertEquals(true, denseDates.contains("2024-05-31"))
        assertEquals(true, monthlyDenseDates.contains("2024-05-15"))
        assertEquals(listOf("2024-04-11"), oneOffDates)
    }

    @Test
    fun `weekend handling covers no shift move before and sunday move after`() = runTest {
        val transactionRepository = InMemoryTransactionRepository()
        val accountRepository = InMemoryAccountRepository().apply {
            save(AccountFixtures.account(id = "acc_123", weekendHandling = "NO_SHIFT"))
            save(AccountFixtures.account(id = "acc_456", weekendHandling = "MOVE_BEFORE"))
            save(AccountFixtures.account(id = "acc_789", weekendHandling = "MOVE_AFTER"))
        }
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(RecurringTransactionFixtures.recurringTransaction(id = "sat_no_shift", accountId = "acc_123", firstOccurrenceDate = "2024-04-13", finalOccurrenceDate = "2024-04-13", recurrenceType = "NONE", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null))
            save(RecurringTransactionFixtures.recurringTransaction(id = "sat_move_before", accountId = "acc_456", firstOccurrenceDate = "2024-04-13", finalOccurrenceDate = "2024-04-13", recurrenceType = "NONE", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null))
            save(RecurringTransactionFixtures.recurringTransaction(id = "sun_move_before", accountId = "acc_456", firstOccurrenceDate = "2024-04-14", finalOccurrenceDate = "2024-04-14", recurrenceType = "NONE", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null))
            save(RecurringTransactionFixtures.recurringTransaction(id = "sun_move_after", accountId = "acc_789", firstOccurrenceDate = "2024-04-14", finalOccurrenceDate = "2024-04-14", recurrenceType = "NONE", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null))
        }
        val materializer = materializer(recurringRepository, transactionRepository, accountRepository)

        materializer.materializeAll()

        assertEquals("2024-04-13", transactionRepository.all().first { it.recurringTransactionId == "sat_no_shift" }.transactionDate)
        assertEquals("2024-04-12", transactionRepository.all().first { it.recurringTransactionId == "sat_move_before" }.transactionDate)
        assertEquals("2024-04-12", transactionRepository.all().first { it.recurringTransactionId == "sun_move_before" }.transactionDate)
        assertEquals("2024-04-15", transactionRepository.all().first { it.recurringTransactionId == "sun_move_after" }.transactionDate)
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
    fun `shifted duplicate dates are only materialized once`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(RecurringTransactionFixtures.recurringTransaction(id = "shifted", accountId = "acc_456", firstOccurrenceDate = "2024-04-13", finalOccurrenceDate = "2024-04-14", recurrenceType = "DAILY", daysOfWeek = null, weeksOfMonth = null, daysOfMonth = null, monthsOfYear = null))
        }
        val transactionRepository = InMemoryTransactionRepository()
        val accountRepository = InMemoryAccountRepository().apply { save(AccountFixtures.account(id = "acc_456", weekendHandling = "MOVE_BEFORE")) }
        val materializer = materializer(recurringRepository, transactionRepository, accountRepository, today = LocalDate.parse("2024-04-10"))

        assertEquals(1, materializer.materializeAll().createdCount)
        assertEquals(listOf("2024-04-12"), transactionRepository.all().map { it.transactionDate })
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

    private suspend fun materializer(
        recurringRepository: InMemoryRecurringTransactionRepository,
        transactionRepository: InMemoryTransactionRepository,
        accountRepository: InMemoryAccountRepository? = null,
        today: LocalDate = LocalDate.parse("2024-04-10"),
    ): RecurringTransactionMaterializer {
        val resolvedAccountRepository = accountRepository ?: InMemoryAccountRepository().apply {
            save(AccountFixtures.account(weekendHandling = "NO_SHIFT"))
        }
        return RecurringTransactionMaterializer(
            recurringTransactionRepository = recurringRepository,
            transactionRepository = transactionRepository,
            accountRepository = resolvedAccountRepository,
            transactionIdGenerator = { "txn_${transactionRepository.all().size + 1}" },
            localDateProvider = { today },
            timeProvider = { 1L },
            dirtyScopeService = ProjectionDirtyScopeService(InMemoryProjectionDirtyScopeRepository(), timeProvider = { 1L }),
        )
    }
}
