package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.query.pockets.api.dto.PocketQueryResponse
import de.chennemann.plannr.server.pockets.usecases.GetPocketQuery
import de.chennemann.plannr.server.query.pockets.api.PocketQueryApi
import de.chennemann.plannr.server.transactions.api.toResponse
import de.chennemann.plannr.server.query.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.api.dto.PocketTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.usecases.ListPocketFutureTransactionFeed
import de.chennemann.plannr.server.transactions.usecases.ListPocketTransactionFeed
import org.springframework.web.bind.annotation.RestController

@RestController
class PocketQueryController(
    private val getPocketQuery: GetPocketQuery,
    private val listPocketTransactionFeed: ListPocketTransactionFeed,
    private val listPocketFutureTransactionFeed: ListPocketFutureTransactionFeed,
) : PocketQueryApi {
    override suspend fun getById(id: String): PocketQueryResponse =
        getPocketQuery(id).toResponse()

    override suspend fun listTransactions(
        id: String,
        limit: Int,
        before: Long?,
    ): PocketTransactionFeedPageResponse =
        listPocketTransactionFeed(id, before, limit).toResponse()

    override suspend fun listFutureTransactions(
        id: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): PocketFutureTransactionFeedPageResponse =
        listPocketFutureTransactionFeed(id, fromDate, toDate, after, limit).toResponse()
}
