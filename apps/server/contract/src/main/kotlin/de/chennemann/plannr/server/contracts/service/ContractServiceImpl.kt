package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.api.dto.Contract
import de.chennemann.plannr.server.contracts.api.dto.CreateContractCommand
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.domain.upsert
import de.chennemann.plannr.server.contracts.persistence.ContractModel
import de.chennemann.plannr.server.contracts.persistence.toDTO
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.service.CreatePocketForContractCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionChangeEvent
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionEventQueue
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class ContractServiceImpl(
    private val contractRepository: ContractRepository,
    private val partnerService: PartnerService,
    private val pocketService: PocketService,
    private val projectionEventQueue: TransactionProjectionEventQueue? = null,
) : ContractService {
    override suspend fun create(command: CreateContractCommand): Contract {
        val partnerId = resolvePartnerId(command.partnerId)
        val pocket = pocketService.createForContract(
            CreatePocketForContractCommand(
                accountId = command.accountId,
                name = command.name,
                description = command.pocket.description,
                color = command.pocket.color,
                useDefaultPocket = command.pocket.useDefaultPocket,
            ),
        )
        if (contractRepository.findById(pocket.id) != null) {
            throw ConflictException("conflict", "Contract already exists for pocket", mapOf("pocketId" to pocket.id))
        }

        contractRepository.upsert(
            ContractModel(
                pocketId = pocket.id,
                partnerId = partnerId,
                signingDate = command.signingDate,
                expirationDate = command.expirationDate,
                lastCancellationDate = command.lastCancellationDate,
            ),
        )
        enqueueProjectionChange(pocket.id)
        return existingContract(pocket.id)
    }

    override suspend fun update(command: UpdateContractCommand): Contract {
        val existing = existingContract(command.id)
        val partnerId = resolvePartnerId(command.partnerId)
        contractRepository.upsert(
            ContractModel(
                pocketId = existing.id,
                partnerId = partnerId,
                signingDate = command.signingDate,
                expirationDate = command.expirationDate,
                lastCancellationDate = command.lastCancellationDate,
            ),
        )
        enqueueProjectionChange(existing.id)
        return existingContract(existing.id)
    }

    override suspend fun list(accountId: Long?, archived: Boolean): List<Contract> =
        contractRepository.findAllWithPocketsByAccountIdAndArchived(accountId, archived)
            .toList()
            .map { it.toDTO() }

    override suspend fun getById(id: Long): Contract? =
        contractRepository.findWithPocketByPocketId(id)?.toDTO()

    private suspend fun existingContract(id: Long): Contract =
        getById(id)
            ?: throw NotFoundException("not_found", "Contract not found", mapOf("id" to id))

    private suspend fun resolvePartnerId(partnerId: Long?): Long? =
        partnerId?.let {
            partnerService.getById(it)?.id
                ?: throw NotFoundException("not_found", "Partner not found", mapOf("id" to it))
        }

    private suspend fun enqueueProjectionChange(id: Long) {
        projectionEventQueue?.enqueue(
            TransactionProjectionChangeEvent.PocketChanged(id),
        )
    }
}
