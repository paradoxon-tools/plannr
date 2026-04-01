package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository
import org.springframework.stereotype.Component

interface UnarchiveRecurringTransaction {
    suspend operator fun invoke(id: String): RecurringTransaction
}

@Component
internal class UnarchiveRecurringTransactionUseCase(
    private val recurringTransactionRepository: RecurringTransactionRepository,
) : UnarchiveRecurringTransaction {
    override suspend fun invoke(id: String): RecurringTransaction {
        val existing = recurringTransactionRepository.findById(id.trim())
            ?: throw NotFoundException("not_found", "Recurring transaction not found", mapOf("id" to id.trim()))
        val updated = existing.copy(isArchived = false)
        return recurringTransactionRepository.update(updated)
    }
}
