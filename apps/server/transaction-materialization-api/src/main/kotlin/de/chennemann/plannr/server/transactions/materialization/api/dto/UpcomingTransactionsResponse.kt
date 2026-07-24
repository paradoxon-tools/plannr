package de.chennemann.plannr.server.transactions.materialization.api.dto

data class UpcomingTransactionsResponse(
    val afterDate: String,
    val transactions: List<UpcomingTransactionItem>,
    val hasMore: Boolean,
)

data class UpcomingTransactionItem(
    val transactionTemplateId: Long,
    val occurrenceDate: String,
    val sourcePocketId: Long?,
    val destinationPocketId: Long?,
    val financialProfileId: Long,
    val partnerId: Long?,
    val type: String,
    val title: String,
    val description: String?,
    val amount: Long,
    val currencyCode: String,
)
