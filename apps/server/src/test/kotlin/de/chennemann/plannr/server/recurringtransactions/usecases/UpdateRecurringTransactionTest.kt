package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.currencies.support.CurrencyFixtures
import de.chennemann.plannr.server.currencies.support.InMemoryCurrencyRepository
import de.chennemann.plannr.server.currencies.support.InMemoryCurrencyTemplateCatalog
import de.chennemann.plannr.server.currencies.usecases.EnsureCurrencyExistsUseCase
import de.chennemann.plannr.server.partners.support.InMemoryPartnerRepository
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.recurringtransactions.support.InMemoryRecurringTransactionRepository
import de.chennemann.plannr.server.recurringtransactions.support.RecurringTransactionFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateRecurringTransactionTest {
    @Test
    fun `overwrites existing recurring transaction`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction()) }
        val useCase = useCase(recurringRepository)

        val updated = useCase(RecurringTransactionFixtures.updateRequest(title = "Updated").toCommand(RecurringTransactionFixtures.DEFAULT_ID))

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
                ),
            )
        }
        val useCase = useCase(recurringRepository)

        val created = useCase(
            RecurringTransactionFixtures.updateRequest(
                updateMode = "new_version",
                firstOccurrenceDate = "2024-06-15",
                finalOccurrenceDate = null,
                recurrenceType = "MONTHLY",
                daysOfMonth = listOf(15),
                weeksOfMonth = null,
                daysOfWeek = null,
                monthsOfYear = null,
            ).toCommand(RecurringTransactionFixtures.DEFAULT_ID),
        )

        assertEquals("rtx_new", created.id)
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
                ),
            )
        }
        val useCase = useCase(recurringRepository)

        useCase(
            RecurringTransactionFixtures.updateRequest(
                updateMode = "new_version",
                firstOccurrenceDate = "2024-01-10",
                finalOccurrenceDate = null,
                recurrenceType = "WEEKLY",
                daysOfWeek = listOf("MONDAY", "WEDNESDAY"),
                weeksOfMonth = null,
                daysOfMonth = null,
                monthsOfYear = null,
            ).toCommand(RecurringTransactionFixtures.DEFAULT_ID),
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
                ),
            )
        }
        val useCase = useCase(recurringRepository)

        useCase(
            RecurringTransactionFixtures.updateRequest(
                updateMode = "new_version",
                firstOccurrenceDate = "2027-02-28",
                finalOccurrenceDate = null,
                recurrenceType = "YEARLY",
                daysOfMonth = listOf(28),
                weeksOfMonth = null,
                daysOfWeek = null,
                monthsOfYear = listOf(2),
            ).toCommand(RecurringTransactionFixtures.DEFAULT_ID),
        )

        assertEquals("2026-02-28", recurringRepository.findById(RecurringTransactionFixtures.DEFAULT_ID)?.finalOccurrenceDate)
    }

    @Test
    fun `rejects overlapping or branching versions`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply {
            save(RecurringTransactionFixtures.recurringTransaction(firstOccurrenceDate = "2024-01-15", finalOccurrenceDate = null, daysOfMonth = listOf(15), weeksOfMonth = null, daysOfWeek = null, monthsOfYear = null))
            save(RecurringTransactionFixtures.recurringTransaction(id = "rtx_child", previousVersionId = RecurringTransactionFixtures.DEFAULT_ID, firstOccurrenceDate = "2024-06-15"))
        }
        val useCase = useCase(recurringRepository)

        assertFailsWith<ValidationException> {
            useCase(
                RecurringTransactionFixtures.updateRequest(
                    updateMode = "new_version",
                    firstOccurrenceDate = "2024-05-15",
                    daysOfMonth = listOf(15),
                    weeksOfMonth = null,
                    daysOfWeek = null,
                    monthsOfYear = null,
                ).toCommand(RecurringTransactionFixtures.DEFAULT_ID),
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
                ),
            )
        }
        val useCase = useCase(recurringRepository)

        val firstVersion = useCase(
            RecurringTransactionFixtures.updateRequest(
                updateMode = "new_version",
                firstOccurrenceDate = "2024-06-15",
                finalOccurrenceDate = null,
                recurrenceType = "MONTHLY",
                daysOfMonth = listOf(15),
                weeksOfMonth = null,
                daysOfWeek = null,
                monthsOfYear = null,
            ).toCommand(RecurringTransactionFixtures.DEFAULT_ID),
        )
        val secondVersion = useCase(
            RecurringTransactionFixtures.updateRequest(
                updateMode = "new_version",
                firstOccurrenceDate = "2024-09-15",
                finalOccurrenceDate = null,
                recurrenceType = "MONTHLY",
                daysOfMonth = listOf(15),
                weeksOfMonth = null,
                daysOfWeek = null,
                monthsOfYear = null,
            ).toCommand(firstVersion.id),
        )

        assertEquals(firstVersion.id, secondVersion.previousVersionId)
        assertEquals("2024-08-15", recurringRepository.findById(firstVersion.id)?.finalOccurrenceDate)
    }

    @Test
    fun `rejects unsupported update mode`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction()) }
        val useCase = useCase(recurringRepository)

        assertFailsWith<ValidationException> {
            useCase(RecurringTransactionFixtures.updateRequest(updateMode = "parallel").toCommand(RecurringTransactionFixtures.DEFAULT_ID))
        }
    }

    @Test
    fun `normalizes final occurrence date from max recurrence count during update`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction()) }
        val useCase = useCase(recurringRepository)

        val updated = useCase(
            RecurringTransactionFixtures.updateRequest(
                firstOccurrenceDate = "2024-01-15",
                finalOccurrenceDate = null,
                recurrenceType = "MONTHLY",
                daysOfMonth = listOf(15),
                weeksOfMonth = null,
                daysOfWeek = null,
                monthsOfYear = null,
                maxRecurrenceCount = 2,
            ).toCommand(RecurringTransactionFixtures.DEFAULT_ID),
        )

        assertEquals("2024-02-15", updated.finalOccurrenceDate)
    }

    private fun useCase(recurringRepository: InMemoryRecurringTransactionRepository): UpdateRecurringTransactionUseCase {
        val pocketRepository = InMemoryPocketRepository().apply { save(PocketFixtures.pocket()) }
        val partnerRepository = InMemoryPartnerRepository().apply { save(PartnerFixtures.partner()) }
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract()) }
        val currencyRepository = InMemoryCurrencyRepository().apply { save(CurrencyFixtures.currency()) }
        return UpdateRecurringTransactionUseCase(
            recurringRepository,
            EnsureCurrencyExistsUseCase(currencyRepository, InMemoryCurrencyTemplateCatalog()),
            contextResolver(pocketRepository, partnerRepository, contractRepository),
            { "rtx_new" },
            { RecurringTransactionFixtures.DEFAULT_CREATED_AT + 1 },
            RecurringTransactionNormalization(),
            RecurringVersioningService(),
        )
    }
}
