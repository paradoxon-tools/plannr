package de.chennemann.plannr.database.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import de.chennemann.plannr.data.Amount
import de.chennemann.plannr.data.Transaction
import de.chennemann.plannr.database.PlannrDB
import de.chennemann.plannr.datetime.atEndOfDay
import de.chennemann.plannr.datetime.atStartOfDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import migrations.Transactions

interface TransactionRepository {
    val transactions: StateFlow<List<Transaction>>

    suspend fun materializeTransactions(transactions: List<Transaction>, templateId: Long, latestTransactionDate: LocalDate, nextOccurrence: LocalDate?)

    companion object {
        operator fun invoke(plannrDB: PlannrDB, applicationScope: CoroutineScope): TransactionRepository =
            CachingTransactionRepository(plannrDB, applicationScope)
    }
}

private class CachingTransactionRepository(
    private val plannrDB: PlannrDB,
    private val applicationScope: CoroutineScope
): TransactionRepository {

    override val transactions: StateFlow<List<Transaction>> =
        plannrDB.transactionsQueries.loadTransactions()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { loadTransactions ->
                loadTransactions
                    .sortedByDescending { it.date }
                    .asDTOs()
            }
            .stateIn(
                scope = applicationScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    override suspend fun materializeTransactions(transactions: List<Transaction>, templateId: Long, latestTransactionDate: LocalDate, nextOccurrence: LocalDate?) {
        plannrDB.transactionsQueries.transaction {
            transactions.forEach { transaction ->
                plannrDB.transactionsQueries.materializeTemplate(
                    title = transaction.title,
                    description = transaction.description,
                    templateId = transaction.templateId,
                    contractId = transaction.contractId,
                    date = transaction.date.atStartOfDay(),
                    amount = transaction.amount.amount,
                    currency = transaction.amount.currency,
                    sourcePocketId = transaction.sourcePocketId,
                    destinationPocketId = transaction.destinationPocketId,
                    partnerId = transaction.partnerId
                )
            }

            plannrDB.transactionsQueries.updateTemplateOccurrence(latestTransactionDate.atEndOfDay(), nextOccurrence?.atStartOfDay(), templateId)
        }
    }
}

private fun List<Transactions>.asDTOs() = map { it.asDTO() }
private fun Transactions.asDTO() = Transaction(
    id = transactionId,
    title = title,
    description = description,
    templateId = templateId,
    contractId = contractId,
    date = date.toLocalDateTime(TimeZone.currentSystemDefault()).date,
    amount = Amount(amount, currency),
    sourcePocketId = sourcePocketId,
    destinationPocketId = destinationPocketId,
    partnerId = partnerId
)