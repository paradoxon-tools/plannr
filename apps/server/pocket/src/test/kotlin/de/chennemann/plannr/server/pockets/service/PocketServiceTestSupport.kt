package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.transactions.recurring.service.RecurringTransactionService

internal object NoOpPocketArchiveCascade : PocketArchiveCascade {
    override suspend fun archiveFor(pocket: Pocket) = Unit

    override suspend fun unarchiveFor(pocket: Pocket) = Unit
}

internal class RecordingPocketArchiveCascade : PocketArchiveCascade {
    val archivedPocketIds = mutableListOf<String>()
    val unarchivedPocketIds = mutableListOf<String>()

    override suspend fun archiveFor(pocket: Pocket) {
        archivedPocketIds += pocket.id
    }

    override suspend fun unarchiveFor(pocket: Pocket) {
        unarchivedPocketIds += pocket.id
    }
}

internal object NoOpRecurringTransactionService : RecurringTransactionService {
    override suspend fun create(command: RecurringTransactionService.CreateCommand) = throw UnsupportedOperationException("Not used")
    override suspend fun update(command: RecurringTransactionService.UpdateCommand) = throw UnsupportedOperationException("Not used")
    override suspend fun archive(id: String) = throw UnsupportedOperationException("Not used")
    override suspend fun unarchive(id: String) = throw UnsupportedOperationException("Not used")
    override suspend fun archiveForAccount(accountId: Long) = Unit
    override suspend fun unarchiveForAccount(accountId: Long) = Unit
    override suspend fun archiveForPocket(accountId: Long, pocketId: String) = Unit
    override suspend fun unarchiveForPocket(accountId: Long, pocketId: String) = Unit
    override suspend fun delete(id: String) = Unit
}

internal fun pocketService(
    repository: InMemoryPocketRepository = InMemoryPocketRepository(),
    archiveCascade: PocketArchiveCascade = NoOpPocketArchiveCascade,
    accountLookup: PocketAccountLookup = PocketAccountLookup { true },
    recurringTransactionService: RecurringTransactionService = NoOpRecurringTransactionService,
): PocketServiceImpl =
    PocketServiceImpl(
        pocketRepository = repository,
        accountLookup = accountLookup,
        archiveCascade = archiveCascade,
        recurringTransactionService = recurringTransactionService,
        timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
    )
