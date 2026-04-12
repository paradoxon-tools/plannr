package de.chennemann.plannr.server.query.transactions.api.dto

data class AccountTransactionFeedPageResponse(
    val items: List<AccountTransactionFeedItemResponse>,
    val nextBefore: Long?,
)
