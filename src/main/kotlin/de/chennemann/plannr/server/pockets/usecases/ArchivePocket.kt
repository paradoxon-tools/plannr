package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository
import org.springframework.stereotype.Component

interface ArchivePocket {
    suspend operator fun invoke(id: String): Pocket
}

@Component
internal class ArchivePocketUseCase(
    private val pocketRepository: PocketRepository,
    private val contractRepository: ContractRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
) : ArchivePocket {
    override suspend fun invoke(id: String): Pocket {
        val existing = pocketRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Pocket not found",
                details = mapOf("id" to id.trim()),
            )

        val updated = existing.copy(isArchived = true)
        pocketRepository.update(updated)
        contractRepository.findByPocketId(updated.id)?.let { contractRepository.update(it.copy(isArchived = true)) }
        recurringTransactionRepository.findAll(accountId = updated.accountId, archived = false)
            .filter { it.sourcePocketId == updated.id || it.destinationPocketId == updated.id }
            .forEach { recurringTransactionRepository.update(it.copy(isArchived = true)) }
        return updated
    }
}
