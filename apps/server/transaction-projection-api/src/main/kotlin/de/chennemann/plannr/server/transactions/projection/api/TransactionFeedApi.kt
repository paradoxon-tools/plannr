package de.chennemann.plannr.server.transactions.projection.api

import de.chennemann.plannr.server.transactions.projection.api.dto.TransactionFeedResponse
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface TransactionFeedApi {
    @GetExchange("/accounts/{id}/feed")
    suspend fun getForAccount(
        @PathVariable id: Long,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): TransactionFeedResponse

    @GetExchange("/pockets/{id}/feed")
    suspend fun getForPocket(
        @PathVariable id: Long,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): TransactionFeedResponse

    @GetExchange("/contracts/{id}/feed")
    suspend fun getForContract(
        @PathVariable id: Long,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): TransactionFeedResponse
}
