package de.chennemann.plannr.server.accounts.api

import de.chennemann.plannr.server.transactions.api.dto.AccountFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.AccountTransactionFeedPageResponse

interface AccountTransactionFeedQuery {
    suspend fun list(accountId: String, limit: Int, before: Long?): AccountTransactionFeedPageResponse
}

interface AccountFutureTransactionFeedQuery {
    suspend fun list(
        accountId: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): AccountFutureTransactionFeedPageResponse
}
