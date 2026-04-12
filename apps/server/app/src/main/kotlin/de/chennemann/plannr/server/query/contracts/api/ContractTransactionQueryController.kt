package de.chennemann.plannr.server.query.contracts.api

import de.chennemann.plannr.server.query.transactions.api.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.api.PocketTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.usecases.ListContractFutureTransactionFeed
import de.chennemann.plannr.server.query.transactions.usecases.ListContractHistoricalTransactionFeed
import org.springframework.web.bind.annotation.RestController

@RestController
class ContractTransactionQueryController(
    private val listContractHistoricalTransactionFeed: ListContractHistoricalTransactionFeed,
    private val listContractFutureTransactionFeed: ListContractFutureTransactionFeed,
) : ContractTransactionQueryApi {
    override suspend fun listTransactions(
        id: String,
        limit: Int,
        before: Long?,
    ): PocketTransactionFeedPageResponse =
        PocketTransactionFeedPageResponse.from(listContractHistoricalTransactionFeed(id, before, limit))

    override suspend fun listFutureTransactions(
        id: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): PocketFutureTransactionFeedPageResponse =
        PocketFutureTransactionFeedPageResponse.from(listContractFutureTransactionFeed(id, fromDate, toDate, after, limit))
}
