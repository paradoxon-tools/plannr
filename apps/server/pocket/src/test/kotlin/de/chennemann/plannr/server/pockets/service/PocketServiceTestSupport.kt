package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.contracts.api.dto.Contract
import de.chennemann.plannr.server.contracts.service.ContractService
import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.transactions.recurring.service.RecurringTransactionService

internal object NoOpContractService : ContractService {
    override suspend fun create(pocket: Pocket, command: CreateContractCommand): Contract = throw UnsupportedOperationException("Not used")
    override suspend fun update(pocket: Pocket, command: UpdateContractCommand): Contract = throw UnsupportedOperationException("Not used")
    override suspend fun archiveForPocket(pocketId: String) = Unit
    override suspend fun unarchiveForPocket(pocketId: String) = Unit
    override suspend fun delete(id: String) = Unit
    override suspend fun list(accountId: Long?, archived: Boolean): List<Contract> = emptyList()
}

internal class RecordingContractService : ContractService {
    val archivedPocketIds = mutableListOf<String>()
    val unarchivedPocketIds = mutableListOf<String>()

    override suspend fun create(pocket: Pocket, command: CreateContractCommand): Contract = throw UnsupportedOperationException("Not used")
    override suspend fun update(pocket: Pocket, command: UpdateContractCommand): Contract = throw UnsupportedOperationException("Not used")

    override suspend fun archiveForPocket(pocketId: String) {
        archivedPocketIds += pocketId
    }

    override suspend fun unarchiveForPocket(pocketId: String) {
        unarchivedPocketIds += pocketId
    }

    override suspend fun delete(id: String) = Unit
    override suspend fun list(accountId: Long?, archived: Boolean): List<Contract> = emptyList()
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
