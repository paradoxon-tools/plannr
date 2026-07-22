package de.chennemann.plannr.server.transactions.templates.api

import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateRequest
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplatesRequest
import de.chennemann.plannr.server.transactions.templates.api.dto.TransactionTemplateResponse
import de.chennemann.plannr.server.transactions.templates.api.dto.UpdateTransactionTemplateRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange
import org.springframework.web.bind.annotation.RequestParam

@HttpExchange("/transactions/templates")
interface TransactionTemplateApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateTransactionTemplateRequest): TransactionTemplateResponse

    @PostExchange("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createBatch(@RequestBody request: CreateTransactionTemplatesRequest): List<TransactionTemplateResponse>

    @PutExchange("/{id}")
    suspend fun update(@PathVariable id: Long, @RequestBody request: UpdateTransactionTemplateRequest): TransactionTemplateResponse

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: Long): TransactionTemplateResponse

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: Long): TransactionTemplateResponse

    @DeleteExchange("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: Long)

    @GetExchange
    suspend fun list(
        @RequestParam(required = false) archived: Boolean?,
    ): List<TransactionTemplateResponse>

    @GetExchange("/{id}")
    suspend fun getById(@PathVariable id: Long): TransactionTemplateResponse
}
