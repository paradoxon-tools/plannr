package de.chennemann.plannr.server.query.transactions.api.dto

import de.chennemann.plannr.server.query.transactions.domain.AccountFutureTransactionFeedItem

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
) {
    companion object {
        fun from(item: AccountFutureTransactionFeedItem) = AccountFutureTransactionFeedItemResponse(
            accountId = item.accountId,
            transactionId = item.transactionId,
            futurePosition = item.futurePosition,
            transactionDate = item.transactionDate,
            type = item.type,
            status = item.status,
            description = item.description,
            transactionAmount = item.transactionAmount,
            signedAmount = item.signedAmount,
            projectedBalanceAfter = item.projectedBalanceAfter,
        )
    }
}
