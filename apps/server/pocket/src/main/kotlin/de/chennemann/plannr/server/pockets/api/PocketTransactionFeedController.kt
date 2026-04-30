package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketTransactionFeedPageResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class PocketTransactionFeedController(
    private val pocketTransactionFeedQuery: PocketTransactionFeedQuery,
    private val pocketFutureTransactionFeedQuery: PocketFutureTransactionFeedQuery,
) : PocketTransactionFeedApi {
    override suspend fun listTransactions(
        id: String,
        limit: Int,
        before: Long?,
    ): PocketTransactionFeedPageResponse =
        pocketTransactionFeedQuery.list(id, limit, before)

    override suspend fun listFutureTransactions(
        id: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): PocketFutureTransactionFeedPageResponse =
        pocketFutureTransactionFeedQuery.list(id, fromDate, toDate, after, limit)
}
