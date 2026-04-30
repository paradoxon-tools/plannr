package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketTransactionFeedPageResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class ContractTransactionFeedController(
    private val contractHistoricalTransactionFeedQuery: ContractHistoricalTransactionFeedQuery,
    private val contractFutureTransactionFeedQuery: ContractFutureTransactionFeedQuery,
) : ContractTransactionFeedApi {
    override suspend fun listTransactions(
        id: String,
        limit: Int,
        before: Long?,
    ): PocketTransactionFeedPageResponse =
        contractHistoricalTransactionFeedQuery.list(id, limit, before)

    override suspend fun listFutureTransactions(
        id: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): PocketFutureTransactionFeedPageResponse =
        contractFutureTransactionFeedQuery.list(id, fromDate, toDate, after, limit)
}
