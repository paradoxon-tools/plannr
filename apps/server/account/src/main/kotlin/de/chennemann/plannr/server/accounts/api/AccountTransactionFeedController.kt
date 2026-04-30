package de.chennemann.plannr.server.accounts.api

import de.chennemann.plannr.server.transactions.api.dto.AccountFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.AccountTransactionFeedPageResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class AccountTransactionFeedController(
    private val accountTransactionFeedQuery: AccountTransactionFeedQuery,
    private val accountFutureTransactionFeedQuery: AccountFutureTransactionFeedQuery,
) : AccountTransactionFeedApi {
    override suspend fun listTransactions(
        id: String,
        limit: Int,
        before: Long?,
    ): AccountTransactionFeedPageResponse =
        accountTransactionFeedQuery.list(id, limit, before)

    override suspend fun listFutureTransactions(
        id: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): AccountFutureTransactionFeedPageResponse =
        accountFutureTransactionFeedQuery.list(id, fromDate, toDate, after, limit)
}
