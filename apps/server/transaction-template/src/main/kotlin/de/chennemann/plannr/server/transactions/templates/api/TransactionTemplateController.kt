package de.chennemann.plannr.server.transactions.templates.api

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateRequest
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplatesRequest
import de.chennemann.plannr.server.transactions.templates.api.dto.TransactionTemplateResponse
import de.chennemann.plannr.server.transactions.templates.api.dto.UpdateTransactionTemplateRequest
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionTemplateController(
    private val transactionTemplateService: TransactionTemplateService,
) : TransactionTemplateApi {
    override suspend fun create(request: CreateTransactionTemplateRequest): TransactionTemplateResponse =
        transactionTemplateService.create(request.toCreateCommand()).toResponse()

    override suspend fun createBatch(request: CreateTransactionTemplatesRequest): List<TransactionTemplateResponse> =
        transactionTemplateService.createBatch(request.templates.map(CreateTransactionTemplateRequest::toCreateCommand))
            .map { it.toResponse() }

    override suspend fun update(id: Long, request: UpdateTransactionTemplateRequest): TransactionTemplateResponse =
        transactionTemplateService.update(request.toUpdateCommand(id)).toResponse()

    override suspend fun archive(id: Long): TransactionTemplateResponse =
        transactionTemplateService.archive(id).toResponse()

    override suspend fun unarchive(id: Long): TransactionTemplateResponse =
        transactionTemplateService.unarchive(id).toResponse()

    override suspend fun delete(id: Long) =
        transactionTemplateService.delete(id)

    override suspend fun list(archived: Boolean?): List<TransactionTemplateResponse> =
        transactionTemplateService.list(archived).map { it.toResponse() }

    override suspend fun getById(id: Long): TransactionTemplateResponse =
        transactionTemplateService.getById(id)?.toResponse()
            ?: throw NotFoundException("not_found", "Transaction template not found", mapOf("id" to id))
}

