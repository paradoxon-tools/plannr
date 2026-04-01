package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.recurringtransactions.support.InMemoryRecurringTransactionRepository
import de.chennemann.plannr.server.recurringtransactions.support.RecurringTransactionFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetRecurringTransactionTest {
    @Test fun `gets by id`() = runTest {
        val repo = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction()) }
        assertEquals(RecurringTransactionFixtures.DEFAULT_ID, GetRecurringTransactionUseCase(repo)(RecurringTransactionFixtures.DEFAULT_ID).id)
    }
    @Test fun `fails for unknown id`() = runTest {
        assertFailsWith<NotFoundException> { GetRecurringTransactionUseCase(InMemoryRecurringTransactionRepository())("missing") }
    }
}
