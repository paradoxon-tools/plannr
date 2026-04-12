package de.chennemann.plannr.server.query.pockets.api

import de.chennemann.plannr.server.query.pockets.api.dto.PocketQueryResponse
import de.chennemann.plannr.server.query.pockets.usecases.GetPocketQuery
import de.chennemann.plannr.server.query.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.api.dto.PocketTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.usecases.ListPocketFutureTransactionFeed
import de.chennemann.plannr.server.query.transactions.usecases.ListPocketTransactionFeed
import org.springframework.web.bind.annotation.RestController

@RestController
class PocketQueryController(
    private val getPocketQuery: GetPocketQuery,
    private val listPocketTransactionFeed: ListPocketTransactionFeed,
    private val listPocketFutureTransactionFeed: ListPocketFutureTransactionFeed,
) : PocketQueryApi {
    override suspend fun getById(id: String): PocketQueryResponse =
        PocketQueryResponse.from(getPocketQuery(id))

    override suspend fun listTransactions(
        id: String,
        limit: Int,
        before: Long?,
    ): PocketTransactionFeedPageResponse =
        PocketTransactionFeedPageResponse.from(listPocketTransactionFeed(id, before, limit))

    override suspend fun listFutureTransactions(
        id: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): PocketFutureTransactionFeedPageResponse =
        PocketFutureTransactionFeedPageResponse.from(listPocketFutureTransactionFeed(id, fromDate, toDate, after, limit))
}
