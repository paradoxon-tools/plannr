package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.recurringtransactions.support.InMemoryRecurringTransactionRepository
import de.chennemann.plannr.server.recurringtransactions.support.RecurringTransactionFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UnarchiveRecurringTransactionTest {
    @Test fun `unarchives`() = runTest {
        val repo = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction(isArchived = true)) }
        val result = UnarchiveRecurringTransactionUseCase(repo)(RecurringTransactionFixtures.DEFAULT_ID)
        assertEquals(false, result.isArchived)
    }
}
