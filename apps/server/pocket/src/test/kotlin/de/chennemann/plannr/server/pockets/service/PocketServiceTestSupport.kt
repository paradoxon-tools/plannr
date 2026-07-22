package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService

internal object NoOpTransactionTemplateService : TransactionTemplateService {
    override suspend fun create(command: TransactionTemplateService.CreateCommand) = throw UnsupportedOperationException("Not used")
    override suspend fun update(command: TransactionTemplateService.UpdateCommand) = throw UnsupportedOperationException("Not used")
    override suspend fun archive(id: Long) = throw UnsupportedOperationException("Not used")
    override suspend fun unarchive(id: Long) = throw UnsupportedOperationException("Not used")
    override suspend fun archiveForPocket(pocketId: Long) = Unit
    override suspend fun unarchiveForPocket(pocketId: Long) = Unit
    override suspend fun delete(id: Long) = Unit
    override suspend fun list(archived: Boolean?): List<TransactionTemplate> = emptyList()
    override suspend fun getById(id: Long) = null
}

internal fun pocketService(
    repository: InMemoryPocketRepository = InMemoryPocketRepository(),
    accountLookup: PocketAccountLookup = PocketAccountLookup { true },
    transactionTemplateService: TransactionTemplateService = NoOpTransactionTemplateService,
): PocketServiceImpl =
    PocketServiceImpl(
        pocketRepository = repository,
        accountLookup = accountLookup,
        transactionTemplateService = transactionTemplateService,
        timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
    )
