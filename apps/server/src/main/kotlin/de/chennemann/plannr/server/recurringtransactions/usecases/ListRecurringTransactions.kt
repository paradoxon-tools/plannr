package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository
import org.springframework.stereotype.Component

interface ListRecurringTransactions {
    suspend operator fun invoke(accountId: String? = null, contractId: String? = null, archived: Boolean = false): List<RecurringTransaction>
}

@Component
internal class ListRecurringTransactionsUseCase(
    private val recurringTransactionRepository: RecurringTransactionRepository,
) : ListRecurringTransactions {
    override suspend fun invoke(accountId: String?, contractId: String?, archived: Boolean): List<RecurringTransaction> =
        recurringTransactionRepository.findAll(accountId?.trim(), contractId?.trim(), archived)
}
