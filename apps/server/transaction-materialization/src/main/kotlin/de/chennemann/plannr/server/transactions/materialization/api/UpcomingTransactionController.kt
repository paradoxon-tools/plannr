package de.chennemann.plannr.server.transactions.materialization.api

import de.chennemann.plannr.server.transactions.materialization.api.dto.UpcomingTransactionsResponse
import de.chennemann.plannr.server.transactions.materialization.service.UpcomingTransactionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UpcomingTransactionController(
    private val upcomingTransactionService: UpcomingTransactionService,
) {
    @GetMapping("/accounts/{id}/upcoming-transactions")
    suspend fun getUpcomingAccountTransactions(
        @PathVariable id: Long,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): UpcomingTransactionsResponse =
        upcomingTransactionService.forAccount(id, cursor, limit)

    @GetMapping("/pockets/{id}/upcoming-transactions")
    suspend fun getUpcomingPocketTransactions(
        @PathVariable id: Long,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): UpcomingTransactionsResponse =
        upcomingTransactionService.forPocket(id, cursor, limit)

    @GetMapping("/contracts/{id}/upcoming-transactions")
    suspend fun getUpcomingContractTransactions(
        @PathVariable id: Long,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): UpcomingTransactionsResponse =
        upcomingTransactionService.forPocket(id, cursor, limit)
}
