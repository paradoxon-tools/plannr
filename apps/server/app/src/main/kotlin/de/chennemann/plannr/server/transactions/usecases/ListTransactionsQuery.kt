package de.chennemann.plannr.server.transactions.usecases

import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import org.springframework.stereotype.Component

interface ListTransactionsQuery {
    suspend operator fun invoke(accountId: String? = null, pocketId: String? = null, archived: Boolean = false): List<TransactionRecord>
}

@Component
internal class ListTransactionsQueryUseCase(
    private val transactionRepository: TransactionRepository,
) : ListTransactionsQuery {
    override suspend fun invoke(accountId: String?, pocketId: String?, archived: Boolean): List<TransactionRecord> =
        transactionRepository.findAll(
            accountId = accountId?.trim()?.takeIf { it.isNotBlank() },
            pocketId = pocketId?.trim()?.takeIf { it.isNotBlank() },
            archived = archived,
        )
}
