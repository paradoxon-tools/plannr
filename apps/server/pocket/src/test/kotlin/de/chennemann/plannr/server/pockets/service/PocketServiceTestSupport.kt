package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.pockets.api.dto.ContractInfo
import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.PocketWithContract
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.transactions.recurring.service.RecurringTransactionService

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

internal object NoOpRecurringTransactionService : RecurringTransactionService {
    override suspend fun create(command: RecurringTransactionService.CreateCommand) = throw UnsupportedOperationException("Not used")
    override suspend fun update(command: RecurringTransactionService.UpdateCommand) = throw UnsupportedOperationException("Not used")
    override suspend fun archive(id: String) = throw UnsupportedOperationException("Not used")
    override suspend fun unarchive(id: String) = throw UnsupportedOperationException("Not used")
    override suspend fun archiveForAccount(accountId: Long) = Unit
    override suspend fun unarchiveForAccount(accountId: Long) = Unit
    override suspend fun archiveForPocket(accountId: Long, pocketId: Long) = Unit
    override suspend fun unarchiveForPocket(accountId: Long, pocketId: Long) = Unit
    override suspend fun delete(id: String) = Unit
}

internal fun pocketService(
    repository: InMemoryPocketRepository = InMemoryPocketRepository(),
    contractService: ContractService = NoOpContractService,
    accountLookup: PocketAccountLookup = PocketAccountLookup { true },
    recurringTransactionService: RecurringTransactionService = NoOpRecurringTransactionService,
): PocketServiceImpl =
    PocketServiceImpl(
        pocketRepository = repository,
        accountLookup = accountLookup,
        contractService = contractService,
        recurringTransactionService = recurringTransactionService,
        timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
    )
