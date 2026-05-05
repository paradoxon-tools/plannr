package de.chennemann.plannr.server.pockets

import de.chennemann.plannr.server.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.toResponse
import de.chennemann.plannr.server.transactions.service.TransactionFeedService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pockets")
class PocketTransactionFeedController(
    private val transactionFeedService: TransactionFeedService,
) {
    @GetMapping("/{id}/transactions")
    suspend fun listTransactions(
        @PathVariable id: String,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(required = false) before: Long?,
    ): PocketTransactionFeedPageResponse =
        transactionFeedService.listPocketTransactions(id, before, limit).toResponse()

    @GetMapping("/{id}/future-transactions")
    suspend fun listFutureTransactions(
        @PathVariable id: String,
        @RequestParam(required = false) fromDate: String?,
        @RequestParam(required = false) toDate: String?,
        @RequestParam(required = false) after: Long?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): PocketFutureTransactionFeedPageResponse =
        transactionFeedService.listPocketFutureTransactions(id, fromDate, toDate, after, limit).toResponse()
}
