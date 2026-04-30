package de.chennemann.plannr.server.accounts

import de.chennemann.plannr.server.accounts.api.AccountFutureTransactionFeedQuery
import de.chennemann.plannr.server.accounts.api.AccountTransactionFeedQuery
import de.chennemann.plannr.server.transactions.api.toResponse
import de.chennemann.plannr.server.transactions.api.dto.AccountFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.AccountTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.usecases.ListAccountFutureTransactionFeed
import de.chennemann.plannr.server.transactions.usecases.ListAccountTransactionFeed
import org.springframework.stereotype.Component

@Component
internal class UseCaseAccountTransactionFeedQuery(
    private val listAccountTransactionFeed: ListAccountTransactionFeed,
) : AccountTransactionFeedQuery {
    override suspend fun list(
        accountId: String,
        limit: Int,
        before: Long?,
    ): AccountTransactionFeedPageResponse =
        listAccountTransactionFeed(accountId, before, limit).toResponse()
}

@Component
internal class UseCaseAccountFutureTransactionFeedQuery(
    private val listAccountFutureTransactionFeed: ListAccountFutureTransactionFeed,
) : AccountFutureTransactionFeedQuery {
    override suspend fun list(
        accountId: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): AccountFutureTransactionFeedPageResponse =
        listAccountFutureTransactionFeed(accountId, fromDate, toDate, after, limit).toResponse()
}
