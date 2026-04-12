package de.chennemann.plannr.server.query.transactions.dto

import de.chennemann.plannr.server.query.transactions.usecases.ListAccountFutureTransactionFeed

data class AccountFutureTransactionFeedPageResponse(
    val items: List<AccountFutureTransactionFeedItemResponse>,
    val nextAfter: Long?,
) {
    companion object {
        fun from(page: ListAccountFutureTransactionFeed.Page) = AccountFutureTransactionFeedPageResponse(
            items = page.items.map(AccountFutureTransactionFeedItemResponse::from),
            nextAfter = page.nextAfter,
        )
    }
}
