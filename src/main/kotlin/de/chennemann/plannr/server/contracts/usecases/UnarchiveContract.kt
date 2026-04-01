package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository
import org.springframework.stereotype.Component

interface UnarchiveContract {
    suspend operator fun invoke(id: String): Contract
}

@Component
internal class UnarchiveContractUseCase(
    private val contractRepository: ContractRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
) : UnarchiveContract {
    override suspend fun invoke(id: String): Contract {
        val existing = contractRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Contract not found",
                details = mapOf("id" to id.trim()),
            )

        val updated = existing.copy(isArchived = false)
        contractRepository.update(updated)
        recurringTransactionRepository.findByContractId(updated.id).forEach { recurringTransactionRepository.update(it.copy(isArchived = false)) }
        return updated
    }
}
