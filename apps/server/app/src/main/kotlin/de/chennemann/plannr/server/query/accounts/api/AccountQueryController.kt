package de.chennemann.plannr.server.query.accounts.api

import de.chennemann.plannr.server.query.accounts.usecases.GetAccountQuery
import de.chennemann.plannr.server.query.transactions.api.AccountFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.api.AccountTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.usecases.ListAccountFutureTransactionFeed
import de.chennemann.plannr.server.query.transactions.usecases.ListAccountTransactionFeed
import org.springframework.web.bind.annotation.RestController

@RestController
class AccountQueryController(
    private val getAccountQuery: GetAccountQuery,
    private val listAccountTransactionFeed: ListAccountTransactionFeed,
    private val listAccountFutureTransactionFeed: ListAccountFutureTransactionFeed,
) : AccountQueryApi {
    override suspend fun getById(id: String): AccountQueryResponse =
        AccountQueryResponse.from(getAccountQuery(id))

    override suspend fun listTransactions(
        id: String,
        limit: Int,
        before: Long?,
    ): AccountTransactionFeedPageResponse =
        AccountTransactionFeedPageResponse.from(listAccountTransactionFeed(id, before, limit))

    override suspend fun listFutureTransactions(
        id: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): AccountFutureTransactionFeedPageResponse =
        AccountFutureTransactionFeedPageResponse.from(listAccountFutureTransactionFeed(id, fromDate, toDate, after, limit))
}
