package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate

interface TransactionMaterializerService {
    suspend fun materialize(operation: MaterializationOperation): List<MaterializedTransaction>
}

interface UpcomingTransactionCache {
    suspend fun getOrRefresh(transactionTemplate: TransactionTemplate): List<String>
    suspend fun refresh(transactionTemplate: TransactionTemplate)
    fun invalidate(transactionTemplateId: Long)
}

sealed interface MaterializationOperation {
    val transactionTemplate: TransactionTemplate

    data class NewTransactionTemplate(
        override val transactionTemplate: TransactionTemplate,
    ) : MaterializationOperation

    data class EndDateChange(
        override val transactionTemplate: TransactionTemplate,
    ) : MaterializationOperation

    data class FullRefresh(
        override val transactionTemplate: TransactionTemplate,
    ) : MaterializationOperation
}

data class MaterializedTransaction(
    val id: Long,
    val transactionTemplateId: Long,
    val transactionDate: String,
    val sourcePocketId: Long?,
    val destinationPocketId: Long?,
    val partnerId: Long?,
    val title: String,
    val description: String?,
    val amount: Long,
    val currencyCode: String,
    val transactionType: String,
    val createdAt: Long,
)
