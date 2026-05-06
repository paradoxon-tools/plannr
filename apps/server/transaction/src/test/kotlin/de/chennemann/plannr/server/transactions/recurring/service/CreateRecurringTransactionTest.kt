package de.chennemann.plannr.server.transactions.recurring.service

import de.chennemann.plannr.server.common.error.ValidationException
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
        val useCase = RecurringTransactionServiceImpl(
            recurringTransactionRepository = recurringRepository,
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
        val useCase = RecurringTransactionServiceImpl(
            recurringTransactionRepository = recurringRepository,
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
        val useCase = RecurringTransactionServiceImpl(
            recurringTransactionRepository = recurringRepository,
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
    fun `fails when transfer uses the same pocket twice`() = runTest {
        val useCase = RecurringTransactionServiceImpl(
            recurringTransactionRepository = InMemoryRecurringTransactionRepository(),
            timeProvider = { RecurringTransactionFixtures.DEFAULT_CREATED_AT },
            normalization = RecurringTransactionNormalization(),
            versioningService = RecurringVersioningService(),
        )

        assertFailsWith<ValidationException> {
            useCase.create(
                RecurringTransactionFixtures.createCommand(
                    transactionType = "TRANSFER",
                    sourcePocketId = 1L,
                    destinationPocketId = 1L,
                ),
            )
        }
    }
}

