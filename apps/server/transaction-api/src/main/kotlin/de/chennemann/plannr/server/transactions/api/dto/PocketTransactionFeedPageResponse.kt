package de.chennemann.plannr.server.transactions.api.dto

data class PocketTransactionFeedPageResponse(
    val items: List<PocketTransactionFeedItemResponse>,
    val nextBefore: Long?,
)
