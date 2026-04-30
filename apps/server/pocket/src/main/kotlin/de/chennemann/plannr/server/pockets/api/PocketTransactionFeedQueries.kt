package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketTransactionFeedPageResponse

interface PocketTransactionFeedQuery {
    suspend fun list(pocketId: String, limit: Int, before: Long?): PocketTransactionFeedPageResponse
}

interface PocketFutureTransactionFeedQuery {
    suspend fun list(
        pocketId: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): PocketFutureTransactionFeedPageResponse
}
