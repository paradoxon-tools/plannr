package de.chennemann.plannr.server.query.contracts.api

import de.chennemann.plannr.server.query.transactions.api.toResponse
import de.chennemann.plannr.server.query.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.api.dto.PocketTransactionFeedPageResponse
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
        listContractHistoricalTransactionFeed(id, before, limit).toResponse()

    override suspend fun listFutureTransactions(
        id: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): PocketFutureTransactionFeedPageResponse =
        listContractFutureTransactionFeed(id, fromDate, toDate, after, limit).toResponse()
}
