package de.chennemann.plannr.server.transactions.recurring.usecases

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.support.FakeCurrencyService
import de.chennemann.plannr.server.support.FakePartnerService
import de.chennemann.plannr.server.transactions.recurring.support.InMemoryRecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.support.RecurringTransactionFixtures
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
        val partnerService = FakePartnerService()
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract()) }
        val useCase = CreateRecurringTransactionUseCase(
            recurringTransactionRepository = recurringRepository,
            currencyService = FakeCurrencyService(),
            contextResolver = contextResolver(pocketRepository, partnerService, contractRepository),
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
        val partnerService = FakePartnerService()
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract()) }
        val useCase = CreateRecurringTransactionUseCase(
            recurringTransactionRepository = recurringRepository,
            currencyService = FakeCurrencyService(),
            contextResolver = contextResolver(pocketRepository, partnerService, contractRepository),
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
    fun `creates yearly recurring transaction and stores null for empty selectors`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository()
        val pocketRepository = InMemoryPocketRepository().apply { save(PocketFixtures.pocket()) }
        val partnerService = FakePartnerService()
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract()) }
        val useCase = CreateRecurringTransactionUseCase(
            recurringTransactionRepository = recurringRepository,
            currencyService = FakeCurrencyService(),
            contextResolver = contextResolver(pocketRepository, partnerService, contractRepository),
            recurringTransactionIdGenerator = { RecurringTransactionFixtures.DEFAULT_ID },
            timeProvider = { RecurringTransactionFixtures.DEFAULT_CREATED_AT },
            normalization = RecurringTransactionNormalization(),
        )

        val created = useCase(
            RecurringTransactionFixtures.createCommand(
                recurrenceType = "YEARLY",
                firstOccurrenceDate = "2024-02-29",
                finalOccurrenceDate = null,
                daysOfWeek = emptyList(),
                weeksOfMonth = emptyList(),
                daysOfMonth = emptyList(),
                monthsOfYear = emptyList(),
                maxRecurrenceCount = 2,
            ),
        )

        assertEquals("YEARLY", created.recurrenceType)
        assertEquals(null, created.daysOfWeek)
        assertEquals(null, created.weeksOfMonth)
        assertEquals(null, created.daysOfMonth)
        assertEquals(null, created.monthsOfYear)
        assertEquals("2025-02-28", created.finalOccurrenceDate)
    }

    @Test
    fun `fails when contract pocket is not referenced`() = runTest {
        val pocketRepository = InMemoryPocketRepository().apply {
            save(PocketFixtures.pocket())
            save(PocketFixtures.pocket(id = "poc_456", accountId = PocketFixtures.DEFAULT_ACCOUNT_ID, name = "Savings"))
        }
        val partnerService = FakePartnerService()
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract()) }
        val useCase = CreateRecurringTransactionUseCase(
            recurringTransactionRepository = InMemoryRecurringTransactionRepository(),
            currencyService = FakeCurrencyService(),
            contextResolver = contextResolver(pocketRepository, partnerService, contractRepository),
            recurringTransactionIdGenerator = { RecurringTransactionFixtures.DEFAULT_ID },
            timeProvider = { RecurringTransactionFixtures.DEFAULT_CREATED_AT },
            normalization = RecurringTransactionNormalization(),
        )

        assertFailsWith<ValidationException> {
            useCase(RecurringTransactionFixtures.createCommand(sourcePocketId = "poc_456"))
        }
    }
}
