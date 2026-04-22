package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.pockets.service.PocketService
import org.springframework.stereotype.Component

interface UpdateContract {
    suspend operator fun invoke(command: Command): Contract

    data class Command(
        val id: String,
        val pocketId: String,
        val partnerId: String?,
        val name: String,
        val startDate: String,
        val endDate: String?,
        val notes: String?,
    )
}

@Component
internal class UpdateContractUseCase(
    private val contractRepository: ContractRepository,
    private val pocketService: PocketService,
    private val partnerService: PartnerService,
) : UpdateContract {
    override suspend fun invoke(command: UpdateContract.Command): Contract {
        val existing = contractRepository.findById(command.id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Contract not found",
                details = mapOf("id" to command.id.trim()),
            )

        val pocketId = command.pocketId.trim()
        val pocket = pocketService.getById(pocketId)
            ?: throw NotFoundException(
                code = "not_found",
                message = "Pocket not found",
                details = mapOf("id" to pocketId),
            )
        val existingForPocket = contractRepository.findByPocketId(pocket.id)
        if (existingForPocket != null && existingForPocket.id != existing.id) {
            throw ConflictException(
                code = "conflict",
                message = "Contract already exists for pocket",
                details = mapOf("pocketId" to pocket.id),
            )
        }

        val partnerId = command.partnerId?.trim()?.takeIf { it.isNotBlank() }?.let {
            partnerService.getById(it)?.id
                ?: throw NotFoundException(
                    code = "not_found",
                    message = "Partner not found",
                    details = mapOf("id" to it),
                )
        }
        val updated = Contract(
            id = existing.id,
            accountId = pocket.accountId,
            pocketId = pocket.id,
            partnerId = partnerId,
            name = command.name,
            startDate = command.startDate,
            endDate = command.endDate,
            notes = command.notes,
            isArchived = existing.isArchived,
            createdAt = existing.createdAt,
        )

        return contractRepository.update(updated)
    }
}
