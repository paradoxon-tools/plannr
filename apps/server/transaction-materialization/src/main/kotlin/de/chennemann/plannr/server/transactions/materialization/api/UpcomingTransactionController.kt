package de.chennemann.plannr.server.transactions.materialization.api

import de.chennemann.plannr.server.transactions.materialization.api.dto.UpcomingTransactionsResponse
import de.chennemann.plannr.server.transactions.materialization.service.UpcomingTransactionService
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
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
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        after: LocalDate?,
        @RequestParam(defaultValue = "50") count: Int,
    ): UpcomingTransactionsResponse =
        upcomingTransactionService.forAccount(id, after, count)

    @GetMapping("/pockets/{id}/upcoming-transactions")
    suspend fun getUpcomingPocketTransactions(
        @PathVariable id: Long,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        after: LocalDate?,
        @RequestParam(defaultValue = "50") count: Int,
    ): UpcomingTransactionsResponse =
        upcomingTransactionService.forPocket(id, after, count)

    @GetMapping("/contracts/{id}/upcoming-transactions")
    suspend fun getUpcomingContractTransactions(
        @PathVariable id: Long,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        after: LocalDate?,
        @RequestParam(defaultValue = "50") count: Int,
    ): UpcomingTransactionsResponse =
        upcomingTransactionService.forPocket(id, after, count)
}
