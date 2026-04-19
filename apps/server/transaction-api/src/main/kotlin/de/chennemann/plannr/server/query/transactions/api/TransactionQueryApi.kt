package de.chennemann.plannr.server.query.transactions.api

import de.chennemann.plannr.server.transactions.api.dto.TransactionResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange("/query/transactions")
interface TransactionQueryApi {
    @GetExchange
    suspend fun list(
        @RequestParam(required = false) accountId: String?,
        @RequestParam(required = false) pocketId: String?,
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<TransactionResponse>
}
