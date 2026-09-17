package de.chennemann.plannr.database.repository

import de.chennemann.plannr.data.Amount
import de.chennemann.plannr.data.Transaction
import de.chennemann.plannr.data.mergeAccountHistory
import de.chennemann.plannr.database.remote.PlannrApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

interface TransactionRepository {
    val transactions: StateFlow<List<Transaction>>
    val history: StateFlow<List<Transaction>>
    val loading: StateFlow<Boolean>
    val error: StateFlow<String?>
    suspend fun refresh()

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
    override val history = MutableStateFlow<List<Transaction>>(emptyList())
    override val loading = MutableStateFlow(true)
    override val error = MutableStateFlow<String?>(null)

    init {
        applicationScope.launchRefresh("TransactionRepository.refresh") {
            refresh()
        }
    }

    override suspend fun materializeTransactions(transactions: List<Transaction>, templateId: Long, latestTransactionDate: LocalDate, nextOccurrence: LocalDate?) {
        refresh()
    }

    override suspend fun refresh() {
        refreshLock.withRefreshLock {
            loading.value = true
            error.value = null
            try {
                val accounts = apiClient.listAccounts()
                val pockets = apiClient.listPockets().associateBy { it.id }
                val partners = apiClient.listPartners().associateBy { it.id }
                transactions.value = accounts.flatMap {
                    apiClient.getUpcomingTransactionsForAccount(it.id).transactions
                }.distinctBy { it.transactionTemplateId to it.occurrenceDate }
                    .sortedBy { it.occurrenceDate }.map { item ->
                        val source = pockets[item.sourcePocketId]
                        val destination = pockets[item.destinationPocketId]
                        Transaction(
                            id = "${item.transactionTemplateId}:${item.occurrenceDate}".hashCode().toLong(),
                            title = item.title, description = item.description,
                            templateId = item.transactionTemplateId, contractId = item.contractId,
                            date = LocalDate.parse(item.occurrenceDate),
                            amount = Amount(item.amount, item.currencyCode),
                            sourcePocketId = item.sourcePocketId, destinationPocketId = item.destinationPocketId,
                            partnerId = item.partnerId,
                            sourceName = source?.name ?: partners[item.partnerId]?.name ?: "External",
                            destinationName = destination?.name ?: partners[item.partnerId]?.name ?: "External",
                            sourceContractId = source?.contractId,
                            destinationContractId = destination?.contractId,
                            signedAmount = when {
                                source != null && destination != null -> 0
                                destination != null -> item.amount
                                else -> -item.amount
                            },
                        )
                    }
                val entries = mutableListOf<Transaction>()
                for (account in accounts) {
                    var cursor: String? = null
                    do {
                        val page = apiClient.getAccountHistory(account.id, cursor)
                        entries += page.transactions.map { item ->
                            Transaction(
                                id = item.transactionId, title = item.title, description = item.description,
                                templateId = item.transactionTemplateId, contractId = null,
                                date = LocalDate.parse(item.transactionDate),
                                amount = Amount(item.transactionAmount, account.currencyCode),
                                sourcePocketId = item.sourcePocket?.id, destinationPocketId = item.destinationPocket?.id,
                                partnerId = item.partner?.id,
                                sourceName = item.sourcePocket?.name ?: item.partner?.name ?: "External",
                                destinationName = item.destinationPocket?.name ?: item.partner?.name ?: "External",
                                signedAmount = item.signedAmount,
                            )
                        }
                        cursor = page.nextCursor.takeIf { page.hasMore }
                    } while (cursor != null)
                }
                history.value = entries.mergeAccountHistory()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                error.value = "Could not load transactions. Please try again."
            } finally {
                loading.value = false
            }
        }
    }

}
