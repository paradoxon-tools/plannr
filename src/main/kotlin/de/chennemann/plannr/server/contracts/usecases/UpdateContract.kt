package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.partners.usecases.GetPartner
import de.chennemann.plannr.server.pockets.usecases.GetPocket
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
    private val getPocket: GetPocket,
    private val getPartner: GetPartner,
) : UpdateContract {
    override suspend fun invoke(command: UpdateContract.Command): Contract {
        val existing = contractRepository.findById(command.id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Contract not found",
                details = mapOf("id" to command.id.trim()),
            )

        val pocket = getPocket(command.pocketId)
        val existingForPocket = contractRepository.findByPocketId(pocket.id)
        if (existingForPocket != null && existingForPocket.id != existing.id) {
            throw ConflictException(
                code = "conflict",
                message = "Contract already exists for pocket",
                details = mapOf("pocketId" to pocket.id),
            )
        }

        val partnerId = command.partnerId?.trim()?.takeIf { it.isNotBlank() }?.let { getPartner(it).id }
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
