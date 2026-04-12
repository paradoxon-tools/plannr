package de.chennemann.plannr.server.query.accounts.api

import de.chennemann.plannr.server.query.accounts.usecases.GetAccountQuery
import de.chennemann.plannr.server.query.transactions.api.AccountFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.api.AccountTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.usecases.ListAccountFutureTransactionFeed
import de.chennemann.plannr.server.query.transactions.usecases.ListAccountTransactionFeed
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/query/accounts")
class AccountQueryController(
    private val getAccountQuery: GetAccountQuery,
    private val listAccountTransactionFeed: ListAccountTransactionFeed,
    private val listAccountFutureTransactionFeed: ListAccountFutureTransactionFeed,
) {
    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: String): AccountQueryResponse =
        AccountQueryResponse.from(getAccountQuery(id))

    @GetMapping("/{id}/transactions")
    suspend fun listTransactions(
        @PathVariable id: String,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(required = false) before: Long?,
    ): AccountTransactionFeedPageResponse =
        AccountTransactionFeedPageResponse.from(listAccountTransactionFeed(id, before, limit))

    @GetMapping("/{id}/future-transactions")
    suspend fun listFutureTransactions(
        @PathVariable id: String,
        @RequestParam(required = false) fromDate: String?,
        @RequestParam(required = false) toDate: String?,
        @RequestParam(required = false) after: Long?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): AccountFutureTransactionFeedPageResponse =
        AccountFutureTransactionFeedPageResponse.from(listAccountFutureTransactionFeed(id, fromDate, toDate, after, limit))
}
