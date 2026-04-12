package de.chennemann.plannr.server.query.transactions.api.dto

data class PocketTransactionFeedPageResponse(
    val items: List<PocketTransactionFeedItemResponse>,
    val nextBefore: Long?,
)
