package de.chennemann.plannr.server.query.accounts.api

import de.chennemann.plannr.server.query.accounts.api.dto.AccountQueryResponse
import de.chennemann.plannr.server.query.accounts.usecases.GetAccountQuery
import de.chennemann.plannr.server.query.transactions.api.toResponse
import de.chennemann.plannr.server.query.transactions.api.dto.AccountFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.api.dto.AccountTransactionFeedPageResponse
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
        getAccountQuery(id).toResponse()

    override suspend fun listTransactions(
        id: String,
        limit: Int,
        before: Long?,
    ): AccountTransactionFeedPageResponse =
        listAccountTransactionFeed(id, before, limit).toResponse()

    override suspend fun listFutureTransactions(
        id: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): AccountFutureTransactionFeedPageResponse =
        listAccountFutureTransactionFeed(id, fromDate, toDate, after, limit).toResponse()
}
