package de.chennemann.plannr.server.transactions.templates.api

import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplatesCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.api.dto.UpdateTransactionTemplateCommand
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/transactions/templates")
interface TransactionTemplateApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody command: CreateTransactionTemplateCommand): TransactionTemplate

    @PostExchange("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createBatch(@RequestBody command: CreateTransactionTemplatesCommand): List<TransactionTemplate>

    @PutExchange
    suspend fun update(@RequestBody command: UpdateTransactionTemplateCommand): TransactionTemplate

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: Long): TransactionTemplate

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: Long): TransactionTemplate

    @DeleteExchange("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: Long)

    @GetExchange
    suspend fun list(
        @RequestParam(required = false) archived: Boolean?,
    ): List<TransactionTemplate>

    @GetExchange("/{id}")
    suspend fun getById(@PathVariable id: Long): TransactionTemplate
}
