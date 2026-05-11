package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.pockets.api.dto.ContractInfo
import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.PocketWithContract
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService

internal object NoOpContractService : ContractService {
    override suspend fun create(pocket: Pocket, command: CreateContractCommand): PocketWithContract = throw UnsupportedOperationException("Not used")
    override suspend fun update(pocket: Pocket, command: UpdateContractCommand): PocketWithContract = throw UnsupportedOperationException("Not used")
    override suspend fun list(accountId: Long?, archived: Boolean): List<PocketWithContract> = emptyList()
}

internal class RecordingContractService : ContractService {
    val createdContracts = mutableListOf<Pair<Pocket, CreateContractCommand>>()

    override suspend fun create(pocket: Pocket, command: CreateContractCommand): PocketWithContract {
        createdContracts += pocket to command
        return PocketWithContract(
            id = createdContracts.size.toLong(),
            accountId = pocket.accountId,
            name = pocket.name,
            description = pocket.description,
            color = pocket.color,
            isDefault = pocket.isDefault,
            isContractPocket = true,
            isArchived = false,
            createdAt = PocketFixtures.DEFAULT_CREATED_AT,
            contractInfo = ContractInfo(command.partnerId, command.signingDate, command.expirationDate, command.lastCancellationDate),
        )
    }
    override suspend fun update(pocket: Pocket, command: UpdateContractCommand): PocketWithContract = throw UnsupportedOperationException("Not used")
    override suspend fun list(accountId: Long?, archived: Boolean): List<PocketWithContract> = emptyList()
}

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
    contractService: ContractService = NoOpContractService,
    accountLookup: PocketAccountLookup = PocketAccountLookup { true },
    transactionTemplateService: TransactionTemplateService = NoOpTransactionTemplateService,
): PocketServiceImpl =
    PocketServiceImpl(
        pocketRepository = repository,
        accountLookup = accountLookup,
        contractService = contractService,
        transactionTemplateService = transactionTemplateService,
        timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
    )
