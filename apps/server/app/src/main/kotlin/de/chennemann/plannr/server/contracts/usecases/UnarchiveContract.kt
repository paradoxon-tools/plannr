package de.chennemann.plannr.server.contracts.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.toModel
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.persistence.toModel
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

        val updated = existing.unarchive()
        contractRepository.update(updated.toModel())
        recurringTransactionRepository.findByContractId(updated.id).forEach { recurringTransactionRepository.update(it.unarchive().toModel()) }
        return updated
    }
}
