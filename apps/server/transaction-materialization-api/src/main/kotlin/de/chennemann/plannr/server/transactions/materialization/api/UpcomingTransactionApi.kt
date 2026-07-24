package de.chennemann.plannr.server.transactions.materialization.api

import de.chennemann.plannr.server.transactions.materialization.api.dto.UpcomingTransactionsResponse
import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface UpcomingTransactionApi {
    @GetExchange("/accounts/{id}/upcoming-transactions")
    suspend fun getForAccount(
        @PathVariable id: Long,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        after: LocalDate?,
        @RequestParam(defaultValue = "50") count: Int,
    ): UpcomingTransactionsResponse

    @GetExchange("/pockets/{id}/upcoming-transactions")
    suspend fun getForPocket(
        @PathVariable id: Long,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        after: LocalDate?,
        @RequestParam(defaultValue = "50") count: Int,
    ): UpcomingTransactionsResponse

    @GetExchange("/contracts/{id}/upcoming-transactions")
    suspend fun getForContract(
        @PathVariable id: Long,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        after: LocalDate?,
        @RequestParam(defaultValue = "50") count: Int,
    ): UpcomingTransactionsResponse
}
