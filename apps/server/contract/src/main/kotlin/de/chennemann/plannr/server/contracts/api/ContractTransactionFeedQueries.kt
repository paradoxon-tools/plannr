package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketTransactionFeedPageResponse

interface ContractHistoricalTransactionFeedQuery {
    suspend fun list(contractId: String, limit: Int, before: Long?): PocketTransactionFeedPageResponse
}

interface ContractFutureTransactionFeedQuery {
    suspend fun list(
        contractId: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): PocketFutureTransactionFeedPageResponse
}
