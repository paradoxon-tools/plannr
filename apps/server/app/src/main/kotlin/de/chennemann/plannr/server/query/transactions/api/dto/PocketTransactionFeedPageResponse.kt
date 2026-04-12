package de.chennemann.plannr.server.query.transactions.api.dto

import de.chennemann.plannr.server.query.transactions.usecases.ListPocketTransactionFeed

data class PocketTransactionFeedPageResponse(
    val items: List<PocketTransactionFeedItemResponse>,
    val nextBefore: Long?,
) {
    companion object {
        fun from(page: ListPocketTransactionFeed.Page): PocketTransactionFeedPageResponse = PocketTransactionFeedPageResponse(
            items = page.items.map(PocketTransactionFeedItemResponse::from),
            nextBefore = page.nextBefore,
        )
    }
}
