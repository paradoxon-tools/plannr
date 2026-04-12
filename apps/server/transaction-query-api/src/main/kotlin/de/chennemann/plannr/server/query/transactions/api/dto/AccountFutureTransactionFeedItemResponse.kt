package de.chennemann.plannr.server.query.transactions.api.dto

data class AccountFutureTransactionFeedItemResponse(
    val accountId: String,
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
