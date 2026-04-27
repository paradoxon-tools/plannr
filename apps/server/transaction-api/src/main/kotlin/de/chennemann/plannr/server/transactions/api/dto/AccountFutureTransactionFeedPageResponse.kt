package de.chennemann.plannr.server.transactions.api.dto

data class AccountFutureTransactionFeedPageResponse(
    val items: List<AccountFutureTransactionFeedItemResponse>,
    val nextAfter: Long?,
)
