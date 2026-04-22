package de.chennemann.plannr.server.transactions.usecases

import de.chennemann.plannr.server.common.time.LocalDateProvider
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import de.chennemann.plannr.server.transactions.domain.accountSignedAmount
import de.chennemann.plannr.server.transactions.domain.pocketSignedAmount
import org.springframework.stereotype.Component

@Component
class CurrentBalanceCalculator(
    private val transactionRepository: TransactionRepository,
    private val localDateProvider: LocalDateProvider,
) {
    suspend fun accountBalance(accountId: String): Long {
        val today = localDateProvider().toString()
        return transactionRepository.findVisibleByAccountId(accountId)
            .filter { it.transactionDate <= today }
            .sumOf { it.accountSignedAmount() }
    }

    suspend fun pocketBalance(pocketId: String): Long {
        val today = localDateProvider().toString()
        return transactionRepository.findVisibleByPocketId(pocketId)
            .filter { it.transactionDate <= today }
            .sumOf { it.pocketSignedAmount(pocketId) }
    }
}
