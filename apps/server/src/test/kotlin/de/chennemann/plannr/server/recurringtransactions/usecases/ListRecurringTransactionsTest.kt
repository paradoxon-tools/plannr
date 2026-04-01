package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.recurringtransactions.support.InMemoryRecurringTransactionRepository
import de.chennemann.plannr.server.recurringtransactions.support.RecurringTransactionFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ListRecurringTransactionsTest {
    @Test fun `filters by account contract and archived`() = runTest {
        val repo = InMemoryRecurringTransactionRepository().apply {
            save(RecurringTransactionFixtures.recurringTransaction(id = "a", contractId = "con_1", accountId = "acc_1"))
            save(RecurringTransactionFixtures.recurringTransaction(id = "b", contractId = "con_2", accountId = "acc_1", isArchived = true))
            save(RecurringTransactionFixtures.recurringTransaction(id = "c", contractId = "con_1", accountId = "acc_2"))
        }
        val result = ListRecurringTransactionsUseCase(repo)(accountId = "acc_1", contractId = "con_1")
        assertEquals(listOf("a"), result.map { it.id })
    }
}
