package de.chennemann.plannr.server.transactions.recurring.service

import de.chennemann.plannr.server.transactions.recurring.support.InMemoryRecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.support.RecurringTransactionFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UnarchiveRecurringTransactionTest {
    @Test fun `unarchives`() = runTest {
        val repo = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction(isArchived = true)) }
        val result = RecurringTransactionServiceImpl(
            recurringTransactionRepository = repo,
            timeProvider = { 1L },
            normalization = RecurringTransactionNormalization(),
            versioningService = RecurringVersioningService(),
        ).unarchive(RecurringTransactionFixtures.DEFAULT_ID)
        assertEquals(false, result.isArchived)
    }
}

