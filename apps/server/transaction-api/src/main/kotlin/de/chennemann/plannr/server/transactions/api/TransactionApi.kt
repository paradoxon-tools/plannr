package de.chennemann.plannr.server.transactions.api

import de.chennemann.plannr.server.transactions.api.dto.CreateTransactionRequest
import de.chennemann.plannr.server.transactions.api.dto.ModifyRecurringOccurrenceRequest
import de.chennemann.plannr.server.transactions.api.dto.TransactionResponse
import de.chennemann.plannr.server.transactions.api.dto.UpdateTransactionRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/transactions")
interface TransactionApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateTransactionRequest): TransactionResponse

    @PutExchange("/{id}")
    suspend fun update(@PathVariable id: String, @RequestBody request: UpdateTransactionRequest): TransactionResponse

    @PostExchange("/{id}/modify-recurring-occurrence")
    suspend fun modifyRecurringOccurrence(@PathVariable id: String, @RequestBody request: ModifyRecurringOccurrenceRequest): TransactionResponse

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: String): TransactionResponse

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): TransactionResponse

    @GetExchange
    suspend fun list(
        @RequestParam(required = false) accountId: String?,
        @RequestParam(required = false) pocketId: String?,
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<TransactionResponse>
}
