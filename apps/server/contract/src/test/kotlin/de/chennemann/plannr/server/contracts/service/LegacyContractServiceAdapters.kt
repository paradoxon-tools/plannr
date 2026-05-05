package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.contracts.api.dto.CreateContractCommand
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.service.PocketService

class CreateContractServiceImpl(
    contractRepository: ContractRepository,
    pocketService: PocketService,
    partnerService: PartnerService,
    timeProvider: TimeProvider,
) {
    private val delegate = ContractServiceImpl(
        contractRepository = contractRepository,
        pocketService = pocketService,
        partnerService = partnerService,
        recurringTransactionCascade = object : ContractRecurringTransactionCascade {
            override suspend fun archiveFor(contract: Contract) = Unit
            override suspend fun unarchiveFor(contract: Contract) = Unit
        },
        timeProvider = timeProvider,
    )

    suspend operator fun invoke(command: CreateContractCommand) =
        delegate.create(command)
}

class UpdateContractServiceImpl(
    contractRepository: ContractRepository,
    pocketService: PocketService,
    partnerService: PartnerService,
) {
    private val delegate = ContractServiceImpl(
        contractRepository = contractRepository,
        pocketService = pocketService,
        partnerService = partnerService,
        recurringTransactionCascade = object : ContractRecurringTransactionCascade {
            override suspend fun archiveFor(contract: Contract) = Unit
            override suspend fun unarchiveFor(contract: Contract) = Unit
        },
        timeProvider = { 0L },
    )

    suspend operator fun invoke(command: UpdateContractCommand) =
        delegate.update(command)
}

class ArchiveContractServiceImpl(
    contractRepository: ContractRepository,
    recurringTransactionCascade: ContractRecurringTransactionCascade,
) {
    private val delegate = ContractServiceImpl(
        contractRepository = contractRepository,
        pocketService = object : PocketService {
            override suspend fun create(command: de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand) = throw UnsupportedOperationException()
            override suspend fun update(command: de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand) = throw UnsupportedOperationException()
            override suspend fun archive(id: String) = throw UnsupportedOperationException()
            override suspend fun unarchive(id: String) = throw UnsupportedOperationException()
            override suspend fun delete(id: String) = throw UnsupportedOperationException()
            override suspend fun list(accountId: String?, archived: Boolean?) = throw UnsupportedOperationException()
            override suspend fun getById(id: String) = throw UnsupportedOperationException()
        },
        partnerService = object : PartnerService {
            override suspend fun create(command: de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand) = throw UnsupportedOperationException()
            override suspend fun update(command: de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand) = throw UnsupportedOperationException()
            override suspend fun archive(id: String) = throw UnsupportedOperationException()
            override suspend fun unarchive(id: String) = throw UnsupportedOperationException()
            override suspend fun delete(id: String) = throw UnsupportedOperationException()
            override suspend fun list(query: String?, archived: Boolean) = throw UnsupportedOperationException()
            override suspend fun getById(id: String) = throw UnsupportedOperationException()
        },
        recurringTransactionCascade = recurringTransactionCascade,
        timeProvider = { 0L },
    )

    suspend operator fun invoke(id: String) =
        delegate.archive(id)
}

class UnarchiveContractServiceImpl(
    contractRepository: ContractRepository,
    recurringTransactionCascade: ContractRecurringTransactionCascade,
) {
    private val delegate = ContractServiceImpl(
        contractRepository = contractRepository,
        pocketService = object : PocketService {
            override suspend fun create(command: de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand) = throw UnsupportedOperationException()
            override suspend fun update(command: de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand) = throw UnsupportedOperationException()
            override suspend fun archive(id: String) = throw UnsupportedOperationException()
            override suspend fun unarchive(id: String) = throw UnsupportedOperationException()
            override suspend fun delete(id: String) = throw UnsupportedOperationException()
            override suspend fun list(accountId: String?, archived: Boolean?) = throw UnsupportedOperationException()
            override suspend fun getById(id: String) = throw UnsupportedOperationException()
        },
        partnerService = object : PartnerService {
            override suspend fun create(command: de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand) = throw UnsupportedOperationException()
            override suspend fun update(command: de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand) = throw UnsupportedOperationException()
            override suspend fun archive(id: String) = throw UnsupportedOperationException()
            override suspend fun unarchive(id: String) = throw UnsupportedOperationException()
            override suspend fun delete(id: String) = throw UnsupportedOperationException()
            override suspend fun list(query: String?, archived: Boolean) = throw UnsupportedOperationException()
            override suspend fun getById(id: String) = throw UnsupportedOperationException()
        },
        recurringTransactionCascade = recurringTransactionCascade,
        timeProvider = { 0L },
    )

    suspend operator fun invoke(id: String) =
        delegate.unarchive(id)
}
