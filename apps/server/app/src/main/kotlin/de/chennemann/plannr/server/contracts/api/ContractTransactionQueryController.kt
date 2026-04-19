package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.query.contracts.api.ContractTransactionQueryApi
import de.chennemann.plannr.server.query.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.api.dto.PocketTransactionFeedPageResponse
import de.chennemann.plannr.server.contracts.api.dto.ContractResponse
import de.chennemann.plannr.server.transactions.api.toResponse
import de.chennemann.plannr.server.contracts.usecases.ListContractsQuery
import de.chennemann.plannr.server.transactions.usecases.ListContractFutureTransactionFeed
import de.chennemann.plannr.server.transactions.usecases.ListContractHistoricalTransactionFeed
import org.springframework.web.bind.annotation.RestController

@RestController
class ContractTransactionQueryController(
    private val listContractsQuery: ListContractsQuery,
    private val listContractHistoricalTransactionFeed: ListContractHistoricalTransactionFeed,
    private val listContractFutureTransactionFeed: ListContractFutureTransactionFeed,
) : ContractTransactionQueryApi {
    override suspend fun list(accountId: String?, archived: Boolean): List<ContractResponse> =
        listContractsQuery(accountId, archived).map { it.toResponse() }

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
