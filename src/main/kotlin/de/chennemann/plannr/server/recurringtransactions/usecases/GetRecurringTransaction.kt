package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository
import org.springframework.stereotype.Component

interface GetRecurringTransaction {
    suspend operator fun invoke(id: String): RecurringTransaction
}

@Component
internal class GetRecurringTransactionUseCase(
    private val recurringTransactionRepository: RecurringTransactionRepository,
) : GetRecurringTransaction {
    override suspend fun invoke(id: String): RecurringTransaction =
        recurringTransactionRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Recurring transaction not found",
                details = mapOf("id" to id.trim()),
            )
}
