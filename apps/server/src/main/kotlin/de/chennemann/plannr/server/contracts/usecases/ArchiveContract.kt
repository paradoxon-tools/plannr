package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository
import org.springframework.stereotype.Component

interface ArchiveContract {
    suspend operator fun invoke(id: String): Contract
}

@Component
internal class ArchiveContractUseCase(
    private val contractRepository: ContractRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
) : ArchiveContract {
    override suspend fun invoke(id: String): Contract {
        val existing = contractRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Contract not found",
                details = mapOf("id" to id.trim()),
            )

        val updated = existing.archive()
        contractRepository.update(updated)
        recurringTransactionRepository.findByContractId(updated.id).forEach { recurringTransactionRepository.update(it.archive()) }
        return updated
    }
}
