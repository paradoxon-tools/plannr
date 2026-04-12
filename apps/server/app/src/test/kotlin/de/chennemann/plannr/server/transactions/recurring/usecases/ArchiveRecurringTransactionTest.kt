package de.chennemann.plannr.server.transactions.recurring.usecases

import de.chennemann.plannr.server.transactions.recurring.support.InMemoryRecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.support.RecurringTransactionFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ArchiveRecurringTransactionTest {
    @Test fun `archives`() = runTest {
        val repo = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction()) }
        val result = ArchiveRecurringTransactionUseCase(repo)(RecurringTransactionFixtures.DEFAULT_ID)
        assertEquals(true, result.isArchived)
    }
}
