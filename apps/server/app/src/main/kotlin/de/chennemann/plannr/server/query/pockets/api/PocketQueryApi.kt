package de.chennemann.plannr.server.query.pockets.api

import de.chennemann.plannr.server.query.transactions.api.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.api.PocketTransactionFeedPageResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange("/query/pockets")
interface PocketQueryApi {
    @GetExchange("/{id}")
    suspend fun getById(@PathVariable id: String): PocketQueryResponse

    @GetExchange("/{id}/transactions")
    suspend fun listTransactions(
        @PathVariable id: String,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(required = false) before: Long?,
    ): PocketTransactionFeedPageResponse

    @GetExchange("/{id}/future-transactions")
    suspend fun listFutureTransactions(
        @PathVariable id: String,
        @RequestParam(required = false) fromDate: String?,
        @RequestParam(required = false) toDate: String?,
        @RequestParam(required = false) after: Long?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): PocketFutureTransactionFeedPageResponse
}
