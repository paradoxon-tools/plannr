package de.chennemann.plannr.server.transactions.api.dto

data class PocketFutureTransactionFeedItemResponse(
    val pocketId: String,
    val accountId: String,
    val contractId: String?,
    val transactionId: String,
    val futurePosition: Long,
    val transactionDate: String,
    val type: String,
    val status: String,
    val description: String,
    val transactionAmount: Long,
    val signedAmount: Long,
    val projectedBalanceAfter: Long,
)
