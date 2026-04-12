package de.chennemann.plannr.server.query.contracts.api

import de.chennemann.plannr.server.query.transactions.api.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.api.PocketTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.usecases.ListContractFutureTransactionFeed
import de.chennemann.plannr.server.query.transactions.usecases.ListContractHistoricalTransactionFeed
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/query/contracts")
class ContractTransactionQueryController(
    private val listContractHistoricalTransactionFeed: ListContractHistoricalTransactionFeed,
    private val listContractFutureTransactionFeed: ListContractFutureTransactionFeed,
) {
    @GetMapping("/{id}/transactions")
    suspend fun listTransactions(
        @PathVariable id: String,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(required = false) before: Long?,
    ): PocketTransactionFeedPageResponse =
        PocketTransactionFeedPageResponse.from(listContractHistoricalTransactionFeed(id, before, limit))

    @GetMapping("/{id}/future-transactions")
    suspend fun listFutureTransactions(
        @PathVariable id: String,
        @RequestParam(required = false) fromDate: String?,
        @RequestParam(required = false) toDate: String?,
        @RequestParam(required = false) after: Long?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): PocketFutureTransactionFeedPageResponse =
        PocketFutureTransactionFeedPageResponse.from(listContractFutureTransactionFeed(id, fromDate, toDate, after, limit))
}
