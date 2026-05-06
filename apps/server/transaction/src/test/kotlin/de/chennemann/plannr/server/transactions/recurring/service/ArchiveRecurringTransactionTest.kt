package de.chennemann.plannr.server.transactions.recurring.service

import de.chennemann.plannr.server.transactions.recurring.support.InMemoryRecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.support.RecurringTransactionFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ArchiveRecurringTransactionTest {
    @Test fun `archives`() = runTest {
        val repo = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction()) }
        val result = RecurringTransactionServiceImpl(
            recurringTransactionRepository = repo,
            contextResolver = contextResolver(
                de.chennemann.plannr.server.support.FakePocketService(),
                de.chennemann.plannr.server.support.FakePartnerService(),
            ),
            timeProvider = { 1L },
            normalization = RecurringTransactionNormalization(),
            versioningService = RecurringVersioningService(),
        ).archive(RecurringTransactionFixtures.DEFAULT_ID)
        assertEquals(true, result.isArchived)
    }
}

