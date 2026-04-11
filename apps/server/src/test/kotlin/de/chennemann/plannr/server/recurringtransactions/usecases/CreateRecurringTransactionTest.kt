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

class CreateRecurringTransactionTest {
    @Test
    fun `creates recurring transaction`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository()
        val pocketRepository = InMemoryPocketRepository().apply {
            save(PocketFixtures.pocket())
        }
        val partnerRepository = InMemoryPartnerRepository().apply { save(PartnerFixtures.partner()) }
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract()) }
        val currencyRepository = InMemoryCurrencyRepository().apply { save(CurrencyFixtures.currency()) }
        val useCase = CreateRecurringTransactionUseCase(
            recurringTransactionRepository = recurringRepository,
            ensureCurrencyExists = EnsureCurrencyExistsUseCase(currencyRepository, InMemoryCurrencyTemplateCatalog()),
            contextResolver = contextResolver(pocketRepository, partnerRepository, contractRepository),
            recurringTransactionIdGenerator = { RecurringTransactionFixtures.DEFAULT_ID },
            timeProvider = { RecurringTransactionFixtures.DEFAULT_CREATED_AT },
            normalization = RecurringTransactionNormalization(),
        )

        val created = useCase(RecurringTransactionFixtures.createCommand())

        assertEquals(RecurringTransactionFixtures.DEFAULT_ID, created.id)
        assertEquals(created, recurringRepository.findById(created.id))
    }

    @Test
    fun `normalizes final occurrence date from max recurrence count`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository()
        val pocketRepository = InMemoryPocketRepository().apply { save(PocketFixtures.pocket()) }
        val partnerRepository = InMemoryPartnerRepository().apply { save(PartnerFixtures.partner()) }
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract()) }
        val currencyRepository = InMemoryCurrencyRepository().apply { save(CurrencyFixtures.currency()) }
        val useCase = CreateRecurringTransactionUseCase(
            recurringTransactionRepository = recurringRepository,
            ensureCurrencyExists = EnsureCurrencyExistsUseCase(currencyRepository, InMemoryCurrencyTemplateCatalog()),
            contextResolver = contextResolver(pocketRepository, partnerRepository, contractRepository),
            recurringTransactionIdGenerator = { RecurringTransactionFixtures.DEFAULT_ID },
            timeProvider = { RecurringTransactionFixtures.DEFAULT_CREATED_AT },
            normalization = RecurringTransactionNormalization(),
        )

        val created = useCase(
            RecurringTransactionFixtures.createCommand(
                finalOccurrenceDate = null,
                recurrenceType = "MONTHLY",
                daysOfMonth = listOf(15),
                weeksOfMonth = null,
                daysOfWeek = null,
                monthsOfYear = null,
                maxRecurrenceCount = 3,
                firstOccurrenceDate = "2024-01-15",
            ),
        )

        assertEquals("2024-03-15", created.finalOccurrenceDate)
    }

    @Test
    fun `fails when contract pocket is not referenced`() = runTest {
        val pocketRepository = InMemoryPocketRepository().apply {
            save(PocketFixtures.pocket())
            save(PocketFixtures.pocket(id = "poc_456", accountId = PocketFixtures.DEFAULT_ACCOUNT_ID, name = "Savings"))
        }
        val partnerRepository = InMemoryPartnerRepository().apply { save(PartnerFixtures.partner()) }
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract()) }
        val currencyRepository = InMemoryCurrencyRepository().apply { save(CurrencyFixtures.currency()) }
        val useCase = CreateRecurringTransactionUseCase(
            recurringTransactionRepository = InMemoryRecurringTransactionRepository(),
            ensureCurrencyExists = EnsureCurrencyExistsUseCase(currencyRepository, InMemoryCurrencyTemplateCatalog()),
            contextResolver = contextResolver(pocketRepository, partnerRepository, contractRepository),
            recurringTransactionIdGenerator = { RecurringTransactionFixtures.DEFAULT_ID },
            timeProvider = { RecurringTransactionFixtures.DEFAULT_CREATED_AT },
            normalization = RecurringTransactionNormalization(),
        )

        assertFailsWith<ValidationException> {
            useCase(RecurringTransactionFixtures.createCommand(sourcePocketId = "poc_456"))
        }
    }
}
