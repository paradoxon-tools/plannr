package de.chennemann.plannr.server.transactions.api.dto

data class PocketFutureTransactionFeedPageResponse(
    val items: List<PocketFutureTransactionFeedItemResponse>,
    val nextAfter: Long?,
)
