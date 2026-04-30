package de.chennemann.plannr.server.pockets

import de.chennemann.plannr.server.pockets.api.PocketFutureTransactionFeedQuery
import de.chennemann.plannr.server.pockets.api.PocketTransactionFeedQuery
import de.chennemann.plannr.server.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.toResponse
import de.chennemann.plannr.server.transactions.usecases.ListPocketFutureTransactionFeed
import de.chennemann.plannr.server.transactions.usecases.ListPocketTransactionFeed
import org.springframework.stereotype.Component

@Component
internal class UseCasePocketTransactionFeedQuery(
    private val listPocketTransactionFeed: ListPocketTransactionFeed,
) : PocketTransactionFeedQuery {
    override suspend fun list(
        pocketId: String,
        limit: Int,
        before: Long?,
    ): PocketTransactionFeedPageResponse =
        listPocketTransactionFeed(pocketId, before, limit).toResponse()
}

@Component
internal class UseCasePocketFutureTransactionFeedQuery(
    private val listPocketFutureTransactionFeed: ListPocketFutureTransactionFeed,
) : PocketFutureTransactionFeedQuery {
    override suspend fun list(
        pocketId: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): PocketFutureTransactionFeedPageResponse =
        listPocketFutureTransactionFeed(pocketId, fromDate, toDate, after, limit).toResponse()
}
