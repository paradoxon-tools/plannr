package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.PocketWithContract
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.pockets.domain.ContractRepository
import de.chennemann.plannr.server.pockets.domain.upsert
import de.chennemann.plannr.server.pockets.persistence.ContractModel
import de.chennemann.plannr.server.pockets.persistence.toDto
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class ContractServiceImpl(
    private val contractRepository: ContractRepository,
    private val partnerService: PartnerService,
) : ContractService {
    override suspend fun create(pocket: Pocket, command: CreateContractCommand): PocketWithContract {
        if (contractRepository.findById(pocket.id) != null) {
            throw ConflictException("conflict", "Contract already exists for pocket", mapOf("pocketId" to pocket.id))
        }

        val partnerId = resolvePartnerId(command.partnerId)
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

        val partnerId = resolvePartnerId(command.partnerId)
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

    override suspend fun list(accountId: Long?, archived: Boolean): List<PocketWithContract> =
        contractRepository.findAllWithPocketsByAccountIdAndArchived(accountId, archived)
            .toList()
            .map { it.toDto() }

    private suspend fun resolvePartnerId(partnerId: Long?): Long? =
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
