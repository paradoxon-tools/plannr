package de.chennemann.plannr.server.transactions.api.dto

data class AccountTransactionFeedPageResponse(
    val items: List<AccountTransactionFeedItemResponse>,
    val nextBefore: Long?,
)
