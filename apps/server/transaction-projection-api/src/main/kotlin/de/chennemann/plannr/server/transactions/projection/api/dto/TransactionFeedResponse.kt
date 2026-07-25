package de.chennemann.plannr.server.transactions.projection.api.dto

data class TransactionFeedResponse(
    val currentBalance: Long,
    val transactions: List<TransactionFeedItem>,
    val nextCursor: String?,
    val hasMore: Boolean,
)

data class TransactionFeedItem(
    val transactionId: Long,
    val transactionTemplateId: Long,
    val historyPosition: Long,
    val transactionDate: String,
    val type: String,
    val title: String,
    val description: String?,
    val transactionAmount: Long,
    val signedAmount: Long,
    val balanceAfter: Long,
    val financialProfile: TransactionFeedFinancialProfileReference,
    val partner: TransactionFeedReference?,
    val sourcePocket: TransactionFeedReference?,
    val destinationPocket: TransactionFeedReference?,
    val transferPocket: TransactionFeedReference?,
    val isArchived: Boolean,
)

data class TransactionFeedFinancialProfileReference(
    val id: Long,
    val name: String,
)

data class TransactionFeedReference(
    val id: Long,
    val name: String,
    val color: Int? = null,
)
