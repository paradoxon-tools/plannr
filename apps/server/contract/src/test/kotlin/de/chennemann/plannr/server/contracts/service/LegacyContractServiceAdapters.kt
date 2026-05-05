package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.service.PocketService

class CreateContractService(
    contractRepository: ContractRepository,
    pocketService: PocketService,
    partnerService: PartnerService,
    timeProvider: TimeProvider,
) {
    private val delegate = ContractService(
        contractRepository = contractRepository,
        pocketService = pocketService,
        partnerService = partnerService,
        recurringTransactionCascade = object : ContractRecurringTransactionCascade {
            override suspend fun archiveFor(contract: Contract) = Unit
            override suspend fun unarchiveFor(contract: Contract) = Unit
        },
        timeProvider = timeProvider,
    )

    suspend operator fun invoke(command: ContractService.CreateCommand) =
        delegate.create(command)
}

class UpdateContractService(
    contractRepository: ContractRepository,
    pocketService: PocketService,
    partnerService: PartnerService,
) {
    private val delegate = ContractService(
        contractRepository = contractRepository,
        pocketService = pocketService,
        partnerService = partnerService,
        recurringTransactionCascade = object : ContractRecurringTransactionCascade {
            override suspend fun archiveFor(contract: Contract) = Unit
            override suspend fun unarchiveFor(contract: Contract) = Unit
        },
        timeProvider = { 0L },
    )

    suspend operator fun invoke(command: ContractService.UpdateCommand) =
        delegate.update(command)
}

class ArchiveContractService(
    contractRepository: ContractRepository,
    recurringTransactionCascade: ContractRecurringTransactionCascade,
) {
    private val delegate = ContractService(
        contractRepository = contractRepository,
        pocketService = object : PocketService {
            override suspend fun create(command: de.chennemann.plannr.server.pockets.service.CreatePocketCommand) = throw UnsupportedOperationException()
            override suspend fun update(command: de.chennemann.plannr.server.pockets.service.UpdatePocketCommand) = throw UnsupportedOperationException()
            override suspend fun archive(id: String) = throw UnsupportedOperationException()
            override suspend fun unarchive(id: String) = throw UnsupportedOperationException()
            override suspend fun list(accountId: String?, archived: Boolean?) = throw UnsupportedOperationException()
            override suspend fun getById(id: String) = throw UnsupportedOperationException()
        },
        partnerService = object : PartnerService {
            override suspend fun create(command: de.chennemann.plannr.server.partners.service.CreatePartnerCommand) = throw UnsupportedOperationException()
            override suspend fun update(command: de.chennemann.plannr.server.partners.service.UpdatePartnerCommand) = throw UnsupportedOperationException()
            override suspend fun archive(id: String) = throw UnsupportedOperationException()
            override suspend fun unarchive(id: String) = throw UnsupportedOperationException()
            override suspend fun list(query: String?, archived: Boolean) = throw UnsupportedOperationException()
            override suspend fun getById(id: String) = throw UnsupportedOperationException()
        },
        recurringTransactionCascade = recurringTransactionCascade,
        timeProvider = { 0L },
    )

    suspend operator fun invoke(id: String) =
        delegate.archive(id)
}

class UnarchiveContractService(
    contractRepository: ContractRepository,
    recurringTransactionCascade: ContractRecurringTransactionCascade,
) {
    private val delegate = ContractService(
        contractRepository = contractRepository,
        pocketService = object : PocketService {
            override suspend fun create(command: de.chennemann.plannr.server.pockets.service.CreatePocketCommand) = throw UnsupportedOperationException()
            override suspend fun update(command: de.chennemann.plannr.server.pockets.service.UpdatePocketCommand) = throw UnsupportedOperationException()
            override suspend fun archive(id: String) = throw UnsupportedOperationException()
            override suspend fun unarchive(id: String) = throw UnsupportedOperationException()
            override suspend fun list(accountId: String?, archived: Boolean?) = throw UnsupportedOperationException()
            override suspend fun getById(id: String) = throw UnsupportedOperationException()
        },
        partnerService = object : PartnerService {
            override suspend fun create(command: de.chennemann.plannr.server.partners.service.CreatePartnerCommand) = throw UnsupportedOperationException()
            override suspend fun update(command: de.chennemann.plannr.server.partners.service.UpdatePartnerCommand) = throw UnsupportedOperationException()
            override suspend fun archive(id: String) = throw UnsupportedOperationException()
            override suspend fun unarchive(id: String) = throw UnsupportedOperationException()
            override suspend fun list(query: String?, archived: Boolean) = throw UnsupportedOperationException()
            override suspend fun getById(id: String) = throw UnsupportedOperationException()
        },
        recurringTransactionCascade = recurringTransactionCascade,
        timeProvider = { 0L },
    )

    suspend operator fun invoke(id: String) =
        delegate.unarchive(id)
}
