package de.chennemann.plannr.server.transactions.api.dto

data class PocketTransactionFeedItemResponse(
    val pocketId: String,
    val accountId: String,
    val contractId: String?,
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
    val transferPocketId: String?,
    val transferPocketName: String?,
    val transferPocketColor: Int?,
    val isArchived: Boolean,
)
