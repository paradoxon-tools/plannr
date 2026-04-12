package de.chennemann.plannr.server.query.transactions.api.dto

data class AccountFutureTransactionFeedPageResponse(
    val items: List<AccountFutureTransactionFeedItemResponse>,
    val nextAfter: Long?,
)
