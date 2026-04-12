package de.chennemann.plannr.server.transactions.recurring.api

import de.chennemann.plannr.server.transactions.recurring.api.dto.CreateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.api.dto.RecurringTransactionResponse
import de.chennemann.plannr.server.transactions.recurring.api.dto.UpdateRecurringTransactionRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/transactions/recurring")
interface RecurringTransactionIngressApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateRecurringTransactionRequest): RecurringTransactionResponse

    @PutExchange("/{id}")
    suspend fun update(@PathVariable id: String, @RequestBody request: UpdateRecurringTransactionRequest): RecurringTransactionResponse

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: String): RecurringTransactionResponse

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): RecurringTransactionResponse
}
