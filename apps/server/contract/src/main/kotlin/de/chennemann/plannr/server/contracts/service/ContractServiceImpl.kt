package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.contracts.api.dto.Contract as ContractDto
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.ContractModel
import de.chennemann.plannr.server.contracts.persistence.toDomain
import de.chennemann.plannr.server.contracts.service.ContractService as ContractServiceApi
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class ContractServiceImpl(
    private val contractRepository: ContractRepository,
    private val partnerService: PartnerService,
    private val timeProvider: TimeProvider,
) : ContractServiceApi {
    override suspend fun create(pocket: Pocket, command: CreateContractCommand): ContractDto {
        if (contractRepository.findByPocketId(pocket.id) != null) {
            throw ConflictException("conflict", "Contract already exists for pocket", mapOf("pocketId" to pocket.id))
        }

        val partnerId = resolvePartnerId(command.partnerId)
        return contractRepository.insert(
            id = null,
            accountId = pocket.accountId,
            pocketId = pocket.id,
            partnerId = partnerId,
            name = command.name,
            startDate = command.startDate,
            endDate = command.endDate,
            notes = command.notes,
            isArchived = false,
            createdAt = timeProvider(),
        ).toDomain().toContractDto()
    }

    override suspend fun update(pocket: Pocket, command: UpdateContractCommand): ContractDto {
        val existing = contractRepository.findById(command.id.trim())?.toDomain()
            ?: throw NotFoundException("not_found", "Contract not found", mapOf("id" to command.id.trim()))

        val existingForPocket = contractRepository.findByPocketId(pocket.id)
        if (existingForPocket != null && existingForPocket.id != existing.id) {
            throw ConflictException("conflict", "Contract already exists for pocket", mapOf("pocketId" to pocket.id))
        }

        val partnerId = resolvePartnerId(command.partnerId)
        return contractRepository.update(
            id = existing.id,
            accountId = pocket.accountId,
            pocketId = pocket.id,
            partnerId = partnerId,
            name = command.name,
            startDate = command.startDate,
            endDate = command.endDate,
            notes = command.notes,
            isArchived = existing.isArchived,
        ).toDomain().toContractDto()
    }

    override suspend fun archiveForPocket(pocketId: String) {
        contractRepository.findByPocketId(pocketId.trim())?.toDomain()?.let { existing ->
            val updated = existing.archive()
            contractRepository.update(
                id = updated.id,
                accountId = updated.accountId,
                pocketId = updated.pocketId,
                partnerId = updated.partnerId,
                name = updated.name,
                startDate = updated.startDate,
                endDate = updated.endDate,
                notes = updated.notes,
                isArchived = true,
            )
        }
    }

    override suspend fun unarchiveForPocket(pocketId: String) {
        contractRepository.findByPocketId(pocketId.trim())?.toDomain()?.let { existing ->
            val updated = existing.unarchive()
            contractRepository.update(
                id = updated.id,
                accountId = updated.accountId,
                pocketId = updated.pocketId,
                partnerId = updated.partnerId,
                name = updated.name,
                startDate = updated.startDate,
                endDate = updated.endDate,
                notes = updated.notes,
                isArchived = false,
            )
        }
    }

    override suspend fun delete(id: String) {
        val normalizedId = id.trim()
        if (contractRepository.findById(normalizedId) == null) {
            throw NotFoundException("not_found", "Contract not found", mapOf("id" to normalizedId))
        }
        contractRepository.deleteById(normalizedId)
    }

    override suspend fun list(accountId: Long?, archived: Boolean): List<ContractDto> =
        contractRepository.findAllByAccountIdAndArchived(accountId, archived)
            .toList()
            .map(ContractModel::toDomain)
            .map { it.toContractDto() }

    private suspend fun resolvePartnerId(partnerId: String?): String? =
        partnerId?.trim()?.takeIf { it.isNotBlank() }?.let {
            partnerService.getById(it)?.id
                ?: throw NotFoundException("not_found", "Partner not found", mapOf("id" to it))
        }

    private fun Contract.toContractDto(): ContractDto =
        ContractDto(
            id = id,
            accountId = accountId,
            pocketId = pocketId,
            partnerId = partnerId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
            isArchived = isArchived,
            createdAt = createdAt,
        )
}
