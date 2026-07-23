package de.chennemann.plannr.server.transactions.materialization.api.dto

data class UpcomingTransactionsResponse(
    val asOfDate: String,
    val transactions: List<UpcomingTransactionItem>,
    val nextCursor: String?,
    val hasMore: Boolean,
)

data class UpcomingTransactionItem(
    val transactionTemplateId: Long,
    val occurrenceDate: String,
    val sourcePocketId: Long?,
    val destinationPocketId: Long?,
    val partnerId: Long?,
    val type: String,
    val title: String,
    val description: String?,
    val amount: Long,
    val currencyCode: String,
)
