package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository
import org.springframework.stereotype.Component

interface UnarchivePocket {
    suspend operator fun invoke(id: String): Pocket
}

@Component
internal class UnarchivePocketUseCase(
    private val pocketRepository: PocketRepository,
    private val contractRepository: ContractRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
) : UnarchivePocket {
    override suspend fun invoke(id: String): Pocket {
        val existing = pocketRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Pocket not found",
                details = mapOf("id" to id.trim()),
            )

        val updated = existing.unarchive()
        pocketRepository.update(updated)
        contractRepository.findByPocketId(updated.id)?.let { contractRepository.update(it.unarchive()) }
        recurringTransactionRepository.findAll(accountId = updated.accountId, archived = true)
            .filter { it.sourcePocketId == updated.id || it.destinationPocketId == updated.id }
            .forEach { recurringTransactionRepository.update(it.unarchive()) }
        return updated
    }
}
