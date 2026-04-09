package de.chennemann.plannr.server.query.pockets.api

import de.chennemann.plannr.server.query.pockets.usecases.GetPocketQuery
import de.chennemann.plannr.server.query.transactions.api.PocketTransactionFeedPageResponse
import de.chennemann.plannr.server.query.transactions.usecases.ListPocketTransactionFeed
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/query/pockets")
class PocketQueryController(
    private val getPocketQuery: GetPocketQuery,
    private val listPocketTransactionFeed: ListPocketTransactionFeed,
) {
    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: String): PocketQueryResponse =
        PocketQueryResponse.from(getPocketQuery(id))

    @GetMapping("/{id}/transactions")
    suspend fun listTransactions(
        @PathVariable id: String,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(required = false) before: Long?,
    ): PocketTransactionFeedPageResponse =
        PocketTransactionFeedPageResponse.from(listPocketTransactionFeed(id, before, limit))
}
