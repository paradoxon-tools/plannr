package de.chennemann.plannr.server.transactions.recurring.service

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.contracts.persistence.toModel
import de.chennemann.plannr.server.contracts.support.ContractFixtures
import de.chennemann.plannr.server.contracts.support.InMemoryContractRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.support.FakePartnerService
import de.chennemann.plannr.server.support.FakePocketService
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
        val pocketService = FakePocketService(listOf(PocketFixtures.pocket()))
        val partnerService = FakePartnerService()
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract().toModel()) }
        val useCase = RecurringTransactionServiceImpl(
            recurringTransactionRepository = recurringRepository,
            contextResolver = contextResolver(pocketService, partnerService, contractRepository),
            timeProvider = { RecurringTransactionFixtures.DEFAULT_CREATED_AT },
            normalization = RecurringTransactionNormalization(),
            versioningService = RecurringVersioningService(),
        )

        val created = useCase.create(RecurringTransactionFixtures.createCommand())

        assertEquals(created, recurringRepository.findById(created.id))
    }

    @Test
    fun `normalizes final occurrence date from max recurrence count`() = runTest {
        val recurringRepository = InMemoryRecurringTransactionRepository()
        val pocketService = FakePocketService(listOf(PocketFixtures.pocket()))
        val partnerService = FakePartnerService()
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract().toModel()) }
        val useCase = RecurringTransactionServiceImpl(
            recurringTransactionRepository = recurringRepository,
            contextResolver = contextResolver(pocketService, partnerService, contractRepository),
            timeProvider = { RecurringTransactionFixtures.DEFAULT_CREATED_AT },
            normalization = RecurringTransactionNormalization(),
            versioningService = RecurringVersioningService(),
        )

        val created = useCase.create(
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
        val pocketService = FakePocketService(listOf(PocketFixtures.pocket()))
        val partnerService = FakePartnerService()
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract().toModel()) }
        val useCase = RecurringTransactionServiceImpl(
            recurringTransactionRepository = recurringRepository,
            contextResolver = contextResolver(pocketService, partnerService, contractRepository),
            timeProvider = { RecurringTransactionFixtures.DEFAULT_CREATED_AT },
            normalization = RecurringTransactionNormalization(),
            versioningService = RecurringVersioningService(),
        )

        val created = useCase.create(
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
        val pocketService = FakePocketService(
            listOf(
                PocketFixtures.pocket(),
                PocketFixtures.pocket(id = "poc_456", accountId = PocketFixtures.DEFAULT_ACCOUNT_ID, name = "Savings"),
            ),
        )
        val partnerService = FakePartnerService()
        val contractRepository = InMemoryContractRepository().apply { save(ContractFixtures.contract().toModel()) }
        val useCase = RecurringTransactionServiceImpl(
            recurringTransactionRepository = InMemoryRecurringTransactionRepository(),
            contextResolver = contextResolver(pocketService, partnerService, contractRepository),
            timeProvider = { RecurringTransactionFixtures.DEFAULT_CREATED_AT },
            normalization = RecurringTransactionNormalization(),
            versioningService = RecurringVersioningService(),
        )

        assertFailsWith<ValidationException> {
            useCase.create(RecurringTransactionFixtures.createCommand(sourcePocketId = "poc_456"))
        }
    }
}

