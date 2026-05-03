package de.chennemann.plannr.server.pockets

import de.chennemann.plannr.server.pockets.api.PocketFutureTransactionFeedQuery
import de.chennemann.plannr.server.pockets.api.PocketTransactionFeedQuery
import de.chennemann.plannr.server.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.toResponse
import de.chennemann.plannr.server.transactions.service.TransactionFeedService
import org.springframework.stereotype.Component

@Component
internal class ServicePocketTransactionFeedQuery(
    private val transactionFeedService: TransactionFeedService,
) : PocketTransactionFeedQuery {
    override suspend fun list(
        pocketId: String,
        limit: Int,
        before: Long?,
    ): PocketTransactionFeedPageResponse =
        transactionFeedService.listPocketTransactions(pocketId, before, limit).toResponse()
}

@Component
internal class ServicePocketFutureTransactionFeedQuery(
    private val transactionFeedService: TransactionFeedService,
) : PocketFutureTransactionFeedQuery {
    override suspend fun list(
        pocketId: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): PocketFutureTransactionFeedPageResponse =
        transactionFeedService.listPocketFutureTransactions(pocketId, fromDate, toDate, after, limit).toResponse()
}

