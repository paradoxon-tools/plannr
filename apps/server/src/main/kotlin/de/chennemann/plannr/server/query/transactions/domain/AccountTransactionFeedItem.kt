package de.chennemann.plannr.server.query.transactions.domain

data class AccountTransactionFeedItem(
    val accountId: String,
    val transactionId: String,
    val historyPosition: Long,
    val transactionDate: String,
    val type: String,
    val status: String,
    val description: String,
    val transactionAmount: Long,
    val signedAmount: Long,
    val balanceAfter: Long,
    val partnerId: String?,
    val partnerName: String?,
    val sourcePocketId: String?,
    val sourcePocketName: String?,
    val sourcePocketColor: Int?,
    val destinationPocketId: String?,
    val destinationPocketName: String?,
    val destinationPocketColor: Int?,
    val isArchived: Boolean,
)
