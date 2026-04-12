package de.chennemann.plannr.server.query.accounts.api

import de.chennemann.plannr.server.query.transactions.api.AccountFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.api.AccountTransactionFeedPageResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange("/query/accounts")
interface AccountQueryApi {
    @GetExchange("/{id}")
    suspend fun getById(@PathVariable id: String): AccountQueryResponse

    @GetExchange("/{id}/transactions")
    suspend fun listTransactions(
        @PathVariable id: String,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(required = false) before: Long?,
    ): AccountTransactionFeedPageResponse

    @GetExchange("/{id}/future-transactions")
    suspend fun listFutureTransactions(
        @PathVariable id: String,
        @RequestParam(required = false) fromDate: String?,
        @RequestParam(required = false) toDate: String?,
        @RequestParam(required = false) after: Long?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): AccountFutureTransactionFeedPageResponse
}
