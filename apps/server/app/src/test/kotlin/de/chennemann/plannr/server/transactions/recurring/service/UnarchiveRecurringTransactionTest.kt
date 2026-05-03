package de.chennemann.plannr.server.transactions.recurring.service

import de.chennemann.plannr.server.transactions.recurring.support.InMemoryRecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.support.RecurringTransactionFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UnarchiveRecurringTransactionTest {
    @Test fun `unarchives`() = runTest {
        val repo = InMemoryRecurringTransactionRepository().apply { save(RecurringTransactionFixtures.recurringTransaction(isArchived = true)) }
        val result = RecurringTransactionService(
            recurringTransactionRepository = repo,
            transactionRepository = de.chennemann.plannr.server.transactions.support.InMemoryTransactionRepository(),
            accountService = de.chennemann.plannr.server.support.FakeAccountService(),
            currencyService = de.chennemann.plannr.server.support.FakeCurrencyService(),
            contextResolver = contextResolver(
                de.chennemann.plannr.server.support.FakePocketService(),
                de.chennemann.plannr.server.support.FakePartnerService(),
                de.chennemann.plannr.server.contracts.support.InMemoryContractRepository(),
            ),
            timeProvider = { 1L },
            localDateProvider = { java.time.LocalDate.parse("2024-04-10") },
            normalization = RecurringTransactionNormalization(),
            versioningService = RecurringVersioningService(),
            projectionPort = object : RecurringTransactionProjectionPort {
                override suspend fun markAccountDirty(accountId: String) = Unit
                override suspend fun markPocketDirty(pocketId: String) = Unit
            },
        ).unarchive(RecurringTransactionFixtures.DEFAULT_ID)
        assertEquals(false, result.isArchived)
    }
}

