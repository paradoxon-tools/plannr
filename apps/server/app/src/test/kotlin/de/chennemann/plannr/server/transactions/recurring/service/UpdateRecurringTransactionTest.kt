package de.chennemann.plannr.server.transactions.recurring.service

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.transactions.recurring.api.toUpdateCommand
import de.chennemann.plannr.server.contracts.persistence.toModel
import de.chennemann.plannr.server.transactions.recurring.persistence.toModel
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.support.FakeCurrencyService
import de.chennemann.plannr.server.support.FakePartnerService
import de.chennemann.plannr.server.support.FakePocketService
import de.chennemann.plannr.server.transactions.recurring.support.InMemoryRecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.support.RecurringTransactionFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateRecurringTransactionTest {
    @Test
    fun `overwrites existing recurring transaction`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction().toModel()) }
        val useCase = useCase(recurringRepository)

        val updated = useCase.update(RecurringTransactionFixtures.updateRequest(title = "Updated").toUpdateCommand(RecurringTransactionFixtures.DEFAULT_ID))

        assertEquals(RecurringTransactionFixtures.DEFAULT_ID, updated.id)
        assertEquals("Updated", updated.title)
    }

    @Test
    fun `creates new recurring version without effective from date`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(
                RecurringTransactionFixtures.recurringTransaction(
                    firstOccurrenceDate = "2024-01-15",
                    finalOccurrenceDate = null,
                    recurrenceType = "MONTHLY",
                    daysOfMonth = listOf(15),
                    weeksOfMonth = null,
                    daysOfWeek = null,
                    monthsOfYear = null,
                ).toModel(),
            )
        }
        val useCase = useCase(recurringRepository)

        val created = useCase.update(
            RecurringTransactionFixtures.updateRequest(
                updateMode = "new_version",
                firstOccurrenceDate = "2024-06-15",
                finalOccurrenceDate = null,
                recurrenceType = "MONTHLY",
                daysOfMonth = listOf(15),
                weeksOfMonth = null,
                daysOfWeek = null,
                monthsOfYear = null,
            ).toUpdateCommand(RecurringTransactionFixtures.DEFAULT_ID),
        )

        assertEquals(RecurringTransactionFixtures.DEFAULT_ID, created.previousVersionId)
        assertEquals("2024-05-15", recurringRepository.findById(RecurringTransactionFixtures.DEFAULT_ID)?.finalOccurrenceDate)
    }

    @Test
    fun `weekly predecessor selection closes old version on prior matching weekday`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(
                RecurringTransactionFixtures.recurringTransaction(
                    firstOccurrenceDate = "2024-01-01",
                    finalOccurrenceDate = null,
                    recurrenceType = "WEEKLY",
                    daysOfWeek = listOf("MONDAY", "WEDNESDAY"),
                    weeksOfMonth = null,
                    daysOfMonth = null,
                    monthsOfYear = null,
                ).toModel(),
            )
        }
        val useCase = useCase(recurringRepository)

        useCase.update(
            RecurringTransactionFixtures.updateRequest(
                updateMode = "new_version",
                firstOccurrenceDate = "2024-01-10",
                finalOccurrenceDate = null,
                recurrenceType = "WEEKLY",
                daysOfWeek = listOf("MONDAY", "WEDNESDAY"),
                weeksOfMonth = null,
                daysOfMonth = null,
                monthsOfYear = null,
            ).toUpdateCommand(RecurringTransactionFixtures.DEFAULT_ID),
        )

        assertEquals("2024-01-08", recurringRepository.findById(RecurringTransactionFixtures.DEFAULT_ID)?.finalOccurrenceDate)
    }

    @Test
    fun `yearly predecessor selection closes old version on prior yearly occurrence`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(
                RecurringTransactionFixtures.recurringTransaction(
                    firstOccurrenceDate = "2024-02-29",
                    finalOccurrenceDate = null,
                    recurrenceType = "YEARLY",
                    daysOfMonth = listOf(29),
                    weeksOfMonth = null,
                    daysOfWeek = null,
                    monthsOfYear = listOf(2),
                ).toModel(),
            )
        }
        val useCase = useCase(recurringRepository)

        useCase.update(
            RecurringTransactionFixtures.updateRequest(
                updateMode = "new_version",
                firstOccurrenceDate = "2027-02-28",
                finalOccurrenceDate = null,
                recurrenceType = "YEARLY",
                daysOfMonth = listOf(28),
                weeksOfMonth = null,
                daysOfWeek = null,
                monthsOfYear = listOf(2),
            ).toUpdateCommand(RecurringTransactionFixtures.DEFAULT_ID),
        )

        assertEquals("2026-02-28", recurringRepository.findById(RecurringTransactionFixtures.DEFAULT_ID)?.finalOccurrenceDate)
    }

    @Test
    fun `rejects overlapping or branching versions`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(RecurringTransactionFixtures.recurringTransaction(firstOccurrenceDate = "2024-01-15", finalOccurrenceDate = null, daysOfMonth = listOf(15), weeksOfMonth = null, daysOfWeek = null, monthsOfYear = null).toModel())
            save(RecurringTransactionFixtures.recurringTransaction(id = "rtx_child", previousVersionId = RecurringTransactionFixtures.DEFAULT_ID, firstOccurrenceDate = "2024-06-15").toModel())
        }
        val useCase = useCase(recurringRepository)

        assertFailsWith<ValidationException> {
            useCase.update(
                RecurringTransactionFixtures.updateRequest(
                    updateMode = "new_version",
                    firstOccurrenceDate = "2024-05-15",
                    daysOfMonth = listOf(15),
                    weeksOfMonth = null,
                    daysOfWeek = null,
                    monthsOfYear = null,
                ).toUpdateCommand(RecurringTransactionFixtures.DEFAULT_ID),
            )
        }
    }

    @Test
    fun `allows sequential non overlapping versions in the same chain`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(
                RecurringTransactionFixtures.recurringTransaction(
                    firstOccurrenceDate = "2024-01-15",
                    finalOccurrenceDate = null,
                    recurrenceType = "MONTHLY",
                    daysOfMonth = listOf(15),
                    weeksOfMonth = null,
                    daysOfWeek = null,
                    monthsOfYear = null,
                ).toModel(),
            )
        }
        val useCase = useCase(recurringRepository)

        val firstVersion = useCase.update(
            RecurringTransactionFixtures.updateRequest(
                updateMode = "new_version",
                firstOccurrenceDate = "2024-06-15",
                finalOccurrenceDate = null,
                recurrenceType = "MONTHLY",
                daysOfMonth = listOf(15),
                weeksOfMonth = null,
                daysOfWeek = null,
                monthsOfYear = null,
            ).toUpdateCommand(RecurringTransactionFixtures.DEFAULT_ID),
        )
        val secondVersion = useCase.update(
            RecurringTransactionFixtures.updateRequest(
                updateMode = "new_version",
                firstOccurrenceDate = "2024-09-15",
                finalOccurrenceDate = null,
                recurrenceType = "MONTHLY",
                daysOfMonth = listOf(15),
                weeksOfMonth = null,
                daysOfWeek = null,
                monthsOfYear = null,
            ).toUpdateCommand(firstVersion.id),
        )

        assertEquals(firstVersion.id, secondVersion.previousVersionId)
        assertEquals("2024-08-15", recurringRepository.findById(firstVersion.id)?.finalOccurrenceDate)
    }

    @Test
    fun `rejects unsupported update mode`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction().toModel()) }
        val useCase = useCase(recurringRepository)

        assertFailsWith<ValidationException> {
            useCase.update(RecurringTransactionFixtures.updateRequest(updateMode = "parallel").toUpdateCommand(RecurringTransactionFixtures.DEFAULT_ID))
        }
    }

    @Test
    fun `normalizes final occurrence date from max recurrence count during update`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction().toModel()) }
        val useCase = useCase(recurringRepository)

        val updated = useCase.update(
            RecurringTransactionFixtures.updateRequest(
                firstOccurrenceDate = "2024-01-15",
                finalOccurrenceDate = null,
                recurrenceType = "MONTHLY",
                daysOfMonth = listOf(15),
                weeksOfMonth = null,
                daysOfWeek = null,
                monthsOfYear = null,
                maxRecurrenceCount = 2,
            ).toUpdateCommand(RecurringTransactionFixtures.DEFAULT_ID),
        )

        assertEquals("2024-02-15", updated.finalOccurrenceDate)
    }

    private suspend fun useCase(recurringRepository: InMemoryRecurringTransactionRepository): RecurringTransactionService {
        val pocketService = FakePocketService(listOf(PocketFixtures.pocket()))
        val partnerService = FakePartnerService()
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract().toModel()) }
        return RecurringTransactionService(
            recurringTransactionRepository = recurringRepository,
            transactionRepository = de.chennemann.plannr.server.transactions.support.InMemoryTransactionRepository(),
            accountService = de.chennemann.plannr.server.support.FakeAccountService(),
            currencyService = FakeCurrencyService(),
            contextResolver = contextResolver(pocketService, partnerService, contractRepository),
            timeProvider = { RecurringTransactionFixtures.DEFAULT_CREATED_AT + 1 },
            localDateProvider = { java.time.LocalDate.parse("2024-04-10") },
            normalization = RecurringTransactionNormalization(),
            versioningService = RecurringVersioningService(),
            projectionPort = object : RecurringTransactionProjectionPort {
                override suspend fun markAccountDirty(accountId: String) = Unit
                override suspend fun markPocketDirty(pocketId: String) = Unit
            },
        )
    }
}

