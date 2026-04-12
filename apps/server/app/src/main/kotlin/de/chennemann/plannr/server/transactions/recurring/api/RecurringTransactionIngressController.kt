package de.chennemann.plannr.server.transactions.recurring.api

import de.chennemann.plannr.server.transactions.recurring.api.dto.CreateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.api.dto.RecurringTransactionResponse
import de.chennemann.plannr.server.transactions.recurring.api.dto.UpdateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.usecases.ArchiveRecurringTransaction
import de.chennemann.plannr.server.transactions.recurring.usecases.CreateRecurringTransaction
import de.chennemann.plannr.server.transactions.recurring.usecases.UnarchiveRecurringTransaction
import de.chennemann.plannr.server.transactions.recurring.usecases.UpdateRecurringTransaction
import org.springframework.web.bind.annotation.RestController

@RestController
class RecurringTransactionIngressController(
    private val createRecurringTransaction: CreateRecurringTransaction,
    private val updateRecurringTransaction: UpdateRecurringTransaction,
    private val archiveRecurringTransaction: ArchiveRecurringTransaction,
    private val unarchiveRecurringTransaction: UnarchiveRecurringTransaction,
) : RecurringTransactionIngressApi {
    override suspend fun create(request: CreateRecurringTransactionRequest): RecurringTransactionResponse =
        createRecurringTransaction(request.toCommand()).toResponse()

    override suspend fun update(id: String, request: UpdateRecurringTransactionRequest): RecurringTransactionResponse =
        updateRecurringTransaction(request.toCommand(id)).toResponse()

    override suspend fun archive(id: String): RecurringTransactionResponse =
        archiveRecurringTransaction(id).toResponse()

    override suspend fun unarchive(id: String): RecurringTransactionResponse =
        unarchiveRecurringTransaction(id).toResponse()
}
