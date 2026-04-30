package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.toDomain
import org.springframework.stereotype.Component

interface ArchiveContract {
    suspend operator fun invoke(id: String): Contract
}

@Component
internal class ArchiveContractUseCase(
    private val contractRepository: ContractRepository,
    private val recurringTransactionCascade: ContractRecurringTransactionCascade,
) : ArchiveContract {
    override suspend fun invoke(id: String): Contract {
        val existing = contractRepository.findById(id.trim())?.toDomain()
            ?: throw NotFoundException(
                code = "not_found",
                message = "Contract not found",
                details = mapOf("id" to id.trim()),
            )

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
        recurringTransactionCascade.archiveFor(updated)
        return updated
    }
}
