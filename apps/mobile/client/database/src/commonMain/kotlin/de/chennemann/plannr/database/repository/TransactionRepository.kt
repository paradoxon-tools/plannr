package de.chennemann.plannr.database.repository

import de.chennemann.plannr.data.Amount
import de.chennemann.plannr.data.Transaction
import de.chennemann.plannr.database.remote.PlannrApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

interface TransactionRepository {
    val transactions: StateFlow<List<Transaction>>

    suspend fun materializeTransactions(transactions: List<Transaction>, templateId: Long, latestTransactionDate: LocalDate, nextOccurrence: LocalDate?)

    companion object {
        internal operator fun invoke(apiClient: PlannrApiClient, applicationScope: CoroutineScope): TransactionRepository =
            RemoteTransactionRepository(apiClient, applicationScope)
    }
}

private class RemoteTransactionRepository(
    private val apiClient: PlannrApiClient,
    private val applicationScope: CoroutineScope
): TransactionRepository {
    private val refreshLock = RefreshLock()

    override val transactions = MutableStateFlow<List<Transaction>>(emptyList())

    init {
        applicationScope.launchRefresh("TransactionRepository.refresh") {
            refresh()
        }
    }

    override suspend fun materializeTransactions(transactions: List<Transaction>, templateId: Long, latestTransactionDate: LocalDate, nextOccurrence: LocalDate?) {
        refresh()
    }

    private suspend fun refresh() {
        refreshLock.withRefreshLock {
            transactions.value = loadTransactions()
        }
    }

    private suspend fun loadTransactions(): List<Transaction> {
        return runCatching {
            apiClient.listAccounts()
                .flatMap { account ->
                    runCatching {
                        apiClient.getUpcomingTransactionsForAccount(account.id).transactions
                    }.getOrDefault(emptyList())
                }
        }.getOrDefault(emptyList())
            .sortedBy { it.occurrenceDate }
            .map { transaction ->
                Transaction(
                    id = "${transaction.transactionTemplateId}:${transaction.occurrenceDate}:${transaction.title}".hashCode().toLong(),
                    title = transaction.title,
                    description = transaction.description,
                    templateId = transaction.transactionTemplateId,
                    contractId = transaction.contractId,
                    date = LocalDate.parse(transaction.occurrenceDate),
                    amount = Amount(transaction.amount, transaction.currencyCode),
                    sourcePocketId = transaction.sourcePocketId,
                    destinationPocketId = transaction.destinationPocketId,
                    partnerId = transaction.partnerId,
                )
            }
    }
}
