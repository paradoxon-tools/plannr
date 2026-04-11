package de.chennemann.plannr.server.recurringtransactions.usecases

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

class UpdateRecurringTransactionTest {
    @Test
    fun `overwrites existing recurring transaction`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction()) }
        val pocketRepository = InMemoryPocketRepository().apply { save(PocketFixtures.pocket()) }
        val partnerRepository = InMemoryPartnerRepository().apply { save(PartnerFixtures.partner()) }
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract()) }
        val currencyRepository = InMemoryCurrencyRepository().apply { save(CurrencyFixtures.currency()) }
        val useCase = UpdateRecurringTransactionUseCase(
            recurringRepository,
            EnsureCurrencyExistsUseCase(currencyRepository, InMemoryCurrencyTemplateCatalog()),
            contextResolver(pocketRepository, partnerRepository, contractRepository),
            { "rtx_new" },
            { RecurringTransactionFixtures.DEFAULT_CREATED_AT + 1 },
            RecurringTransactionNormalization(),
        )

        val updated = useCase(RecurringTransactionFixtures.updateRequest(title = "Updated").toCommand(RecurringTransactionFixtures.DEFAULT_ID))

        assertEquals(RecurringTransactionFixtures.DEFAULT_ID, updated.id)
        assertEquals("Updated", updated.title)
    }

    @Test
    fun `creates parallel recurring transaction`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction()) }
        val pocketRepository = InMemoryPocketRepository().apply { save(PocketFixtures.pocket()) }
        val partnerRepository = InMemoryPartnerRepository().apply { save(PartnerFixtures.partner()) }
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract()) }
        val currencyRepository = InMemoryCurrencyRepository().apply { save(CurrencyFixtures.currency()) }
        val useCase = UpdateRecurringTransactionUseCase(
            recurringRepository,
            EnsureCurrencyExistsUseCase(currencyRepository, InMemoryCurrencyTemplateCatalog()),
            contextResolver(pocketRepository, partnerRepository, contractRepository),
            { "rtx_new" },
            { RecurringTransactionFixtures.DEFAULT_CREATED_AT + 1 },
            RecurringTransactionNormalization(),
        )

        val created = useCase(RecurringTransactionFixtures.updateRequest(updateMode = "parallel", title = "Parallel").toCommand(RecurringTransactionFixtures.DEFAULT_ID))

        assertEquals("rtx_new", created.id)
        assertEquals(RecurringTransactionFixtures.DEFAULT_ID, created.previousVersionId)
    }

    @Test
    fun `normalizes final occurrence date from max recurrence count during update`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction()) }
        val pocketRepository = InMemoryPocketRepository().apply { save(PocketFixtures.pocket()) }
        val partnerRepository = InMemoryPartnerRepository().apply { save(PartnerFixtures.partner()) }
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract()) }
        val currencyRepository = InMemoryCurrencyRepository().apply { save(CurrencyFixtures.currency()) }
        val useCase = UpdateRecurringTransactionUseCase(
            recurringRepository,
            EnsureCurrencyExistsUseCase(currencyRepository, InMemoryCurrencyTemplateCatalog()),
            contextResolver(pocketRepository, partnerRepository, contractRepository),
            { "rtx_new" },
            { RecurringTransactionFixtures.DEFAULT_CREATED_AT + 1 },
            RecurringTransactionNormalization(),
        )

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
}
