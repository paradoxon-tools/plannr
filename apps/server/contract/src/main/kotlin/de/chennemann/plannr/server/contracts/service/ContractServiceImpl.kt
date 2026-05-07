package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.domain.upsert
import de.chennemann.plannr.server.contracts.persistence.ContractModel
import de.chennemann.plannr.server.contracts.persistence.toDto
import de.chennemann.plannr.server.contracts.service.ContractService as ContractServiceApi
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.PocketWithContract
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class ContractServiceImpl(
    private val contractRepository: ContractRepository,
    private val partnerService: PartnerService,
) : ContractServiceApi {
    override suspend fun create(pocket: Pocket, command: CreateContractCommand): PocketWithContract {
        if (contractRepository.findById(pocket.id) != null) {
            throw ConflictException("conflict", "Contract already exists for pocket", mapOf("pocketId" to pocket.id))
        }

        val partnerId = resolvepartnerId(command.partnerId)
        contractRepository.upsert(
            ContractModel(
                pocketId = pocket.id,
                partnerId = partnerId,
                signingDate = command.signingDate,
                expirationDate = command.expirationDate,
                lastCancellationDate = command.lastCancellationDate,
            ),
        )
        return existingPocketWithContract(pocket.id)
    }

    override suspend fun update(pocket: Pocket, command: UpdateContractCommand): PocketWithContract {
        contractRepository.findById(pocket.id)
            ?: throw NotFoundException("not_found", "Contract not found", mapOf("pocketId" to pocket.id))

        val partnerId = resolvepartnerId(command.partnerId)
        contractRepository.upsert(
            ContractModel(
                pocketId = pocket.id,
                partnerId = partnerId,
                signingDate = command.signingDate,
                expirationDate = command.expirationDate,
                lastCancellationDate = command.lastCancellationDate,
            ),
        )
        return existingPocketWithContract(pocket.id)
    }

    override suspend fun archiveForPocket(pocketId: Long) {
        // Contract lifecycle is derived from the owning pocket.
    }

    override suspend fun unarchiveForPocket(pocketId: Long) {
        // Contract lifecycle is derived from the owning pocket.
    }

    override suspend fun delete(pocketId: Long) {
        if (contractRepository.findById(pocketId) == null) {
            throw NotFoundException("not_found", "Contract not found", mapOf("pocketId" to pocketId))
        }
        contractRepository.deleteById(pocketId)
    }

    override suspend fun list(accountId: Long?, archived: Boolean): List<PocketWithContract> =
        contractRepository.findAllWithPocketsByAccountIdAndArchived(accountId, archived)
            .toList()
            .map { it.toDto() }

    private suspend fun resolvepartnerId(partnerId: Long?): Long? =
        partnerId?.let {
            partnerService.getById(it)?.id
                ?: throw NotFoundException("not_found", "Partner not found", mapOf("id" to it))
        }

    private suspend fun existingPocketWithContract(pocketId: Long): PocketWithContract =
        contractRepository.findAllWithPocketsByAccountIdAndArchived(accountId = null, archived = false)
            .toList()
            .firstOrNull { it.id == pocketId }
            ?.toDto()
            ?: contractRepository.findAllWithPocketsByAccountIdAndArchived(accountId = null, archived = true)
                .toList()
                .firstOrNull { it.id == pocketId }
                ?.toDto()
            ?: throw NotFoundException("not_found", "Contract not found", mapOf("pocketId" to pocketId))
}
