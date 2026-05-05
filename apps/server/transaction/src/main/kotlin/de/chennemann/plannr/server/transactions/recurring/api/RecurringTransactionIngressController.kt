package de.chennemann.plannr.server.transactions.recurring.api

import de.chennemann.plannr.server.transactions.recurring.api.dto.CreateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.api.dto.RecurringTransactionResponse
import de.chennemann.plannr.server.transactions.recurring.api.dto.UpdateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.service.RecurringTransactionService
import org.springframework.web.bind.annotation.RestController

@RestController
class RecurringTransactionIngressController(
    private val recurringTransactionService: RecurringTransactionService,
) : RecurringTransactionIngressApi {
    override suspend fun create(request: CreateRecurringTransactionRequest): RecurringTransactionResponse =
        recurringTransactionService.create(request.toCreateCommand()).toResponse()

    override suspend fun update(id: String, request: UpdateRecurringTransactionRequest): RecurringTransactionResponse =
        recurringTransactionService.update(request.toUpdateCommand(id)).toResponse()

    override suspend fun archive(id: String): RecurringTransactionResponse =
        recurringTransactionService.archive(id).toResponse()

    override suspend fun unarchive(id: String): RecurringTransactionResponse =
        recurringTransactionService.unarchive(id).toResponse()

    override suspend fun delete(id: String) =
        recurringTransactionService.delete(id)
}

