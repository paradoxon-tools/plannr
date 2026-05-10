package de.chennemann.plannr.server.transactions.recurring.api

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.transactions.recurring.api.dto.CreateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.api.dto.RecurringTransactionResponse
import de.chennemann.plannr.server.transactions.recurring.api.dto.UpdateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.service.RecurringTransactionService
import org.springframework.web.bind.annotation.RestController

@RestController
class RecurringTransactionController(
    private val recurringTransactionService: RecurringTransactionService,
) : RecurringTransactionApi {
    override suspend fun create(request: CreateRecurringTransactionRequest): RecurringTransactionResponse =
        recurringTransactionService.create(request.toCreateCommand()).toResponse()

    override suspend fun update(id: Long, request: UpdateRecurringTransactionRequest): RecurringTransactionResponse =
        recurringTransactionService.update(request.toUpdateCommand(id)).toResponse()

    override suspend fun archive(id: Long): RecurringTransactionResponse =
        recurringTransactionService.archive(id).toResponse()

    override suspend fun unarchive(id: Long): RecurringTransactionResponse =
        recurringTransactionService.unarchive(id).toResponse()

    override suspend fun delete(id: Long) =
        recurringTransactionService.delete(id)

    override suspend fun list(archived: Boolean?): List<RecurringTransactionResponse> =
        recurringTransactionService.list(archived).map { it.toResponse() }

    override suspend fun getById(id: Long): RecurringTransactionResponse =
        recurringTransactionService.getById(id)?.toResponse()
            ?: throw NotFoundException("not_found", "Recurring transaction not found", mapOf("id" to id))
}

