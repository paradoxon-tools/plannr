package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository
import org.springframework.stereotype.Component

interface ArchiveRecurringTransaction {
    suspend operator fun invoke(id: String): RecurringTransaction
}

@Component
internal class ArchiveRecurringTransactionUseCase(
    private val recurringTransactionRepository: RecurringTransactionRepository,
) : ArchiveRecurringTransaction {
    override suspend fun invoke(id: String): RecurringTransaction {
        val existing = recurringTransactionRepository.findById(id.trim())
            ?: throw NotFoundException("not_found", "Recurring transaction not found", mapOf("id" to id.trim()))
        val updated = existing.copy(isArchived = true)
        return recurringTransactionRepository.update(updated)
    }
}
