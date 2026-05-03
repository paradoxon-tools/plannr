package de.chennemann.plannr.server.transactions.api

import de.chennemann.plannr.server.transactions.api.dto.CreateTransactionRequest
import de.chennemann.plannr.server.transactions.api.dto.ModifyRecurringOccurrenceRequest
import de.chennemann.plannr.server.transactions.api.dto.TransactionResponse
import de.chennemann.plannr.server.transactions.api.dto.UpdateTransactionRequest
import de.chennemann.plannr.server.transactions.service.TransactionService
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionController(
    private val transactionService: TransactionService,
) : TransactionApi {
    override suspend fun create(request: CreateTransactionRequest): TransactionResponse =
        transactionService.create(request.toCreateCommand()).toResponse()

    override suspend fun update(id: String, request: UpdateTransactionRequest): TransactionResponse =
        transactionService.update(request.toUpdateCommand(id)).toResponse()

    override suspend fun modifyRecurringOccurrence(id: String, request: ModifyRecurringOccurrenceRequest): TransactionResponse =
        transactionService.modifyRecurringOccurrence(request.toModifyRecurringOccurrenceCommand(id)).toResponse()

    override suspend fun archive(id: String): TransactionResponse =
        transactionService.archive(id).toResponse()

    override suspend fun unarchive(id: String): TransactionResponse =
        transactionService.unarchive(id).toResponse()

    override suspend fun list(accountId: String?, pocketId: String?, archived: Boolean): List<TransactionResponse> =
        transactionService.list(accountId, pocketId, archived).map { it.toResponse() }
}

