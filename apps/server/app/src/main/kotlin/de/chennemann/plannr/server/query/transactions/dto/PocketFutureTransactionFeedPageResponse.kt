package de.chennemann.plannr.server.query.transactions.dto

import de.chennemann.plannr.server.query.transactions.usecases.ListPocketFutureTransactionFeed

data class PocketFutureTransactionFeedPageResponse(
    val items: List<PocketFutureTransactionFeedItemResponse>,
    val nextAfter: Long?,
) {
    companion object {
        fun from(page: ListPocketFutureTransactionFeed.Page) = PocketFutureTransactionFeedPageResponse(
            items = page.items.map(PocketFutureTransactionFeedItemResponse::from),
            nextAfter = page.nextAfter,
        )
    }
}
