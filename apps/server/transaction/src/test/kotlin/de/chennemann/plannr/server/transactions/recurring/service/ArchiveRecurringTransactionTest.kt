package de.chennemann.plannr.server.transactions.recurring.service

import de.chennemann.plannr.server.transactions.recurring.support.InMemoryRecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.support.RecurringTransactionFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ArchiveRecurringTransactionTest {
    @Test fun `archives`() = runTest {
        val repo = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction()) }
        val result = RecurringTransactionService(
            recurringTransactionRepository = repo,
            contextResolver = contextResolver(
                de.chennemann.plannr.server.support.FakePocketService(),
                de.chennemann.plannr.server.support.FakePartnerService(),
                de.chennemann.plannr.server.contracts.support.InMemoryContractRepository(),
            ),
            timeProvider = { 1L },
            normalization = RecurringTransactionNormalization(),
            versioningService = RecurringVersioningService(),
        ).archive(RecurringTransactionFixtures.DEFAULT_ID)
        assertEquals(true, result.isArchived)
    }
}

