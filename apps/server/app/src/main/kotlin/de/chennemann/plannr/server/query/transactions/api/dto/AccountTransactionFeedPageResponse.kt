package de.chennemann.plannr.server.query.transactions.api.dto

import de.chennemann.plannr.server.query.transactions.usecases.ListAccountTransactionFeed

data class AccountTransactionFeedPageResponse(
    val items: List<AccountTransactionFeedItemResponse>,
    val nextBefore: Long?,
) {
    companion object {
        fun from(page: ListAccountTransactionFeed.Page): AccountTransactionFeedPageResponse = AccountTransactionFeedPageResponse(
            items = page.items.map(AccountTransactionFeedItemResponse::from),
            nextBefore = page.nextBefore,
        )
    }
}
