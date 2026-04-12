package de.chennemann.plannr.server.recurringtransactions.api

import de.chennemann.plannr.server.recurringtransactions.api.dto.CreateRecurringTransactionRequest
import de.chennemann.plannr.server.recurringtransactions.api.dto.RecurringTransactionResponse
import de.chennemann.plannr.server.recurringtransactions.api.dto.UpdateRecurringTransactionRequest
import de.chennemann.plannr.server.recurringtransactions.usecases.ArchiveRecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.usecases.CreateRecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.usecases.UnarchiveRecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.usecases.UpdateRecurringTransaction
import org.springframework.web.bind.annotation.RestController

@RestController
class RecurringTransactionIngressController(
    private val createRecurringTransaction: CreateRecurringTransaction,
    private val updateRecurringTransaction: UpdateRecurringTransaction,
    private val archiveRecurringTransaction: ArchiveRecurringTransaction,
    private val unarchiveRecurringTransaction: UnarchiveRecurringTransaction,
) : RecurringTransactionIngressApi {
    override suspend fun create(request: CreateRecurringTransactionRequest): RecurringTransactionResponse =
        RecurringTransactionResponse.from(createRecurringTransaction(request.toCommand()))

    override suspend fun update(id: String, request: UpdateRecurringTransactionRequest): RecurringTransactionResponse =
        RecurringTransactionResponse.from(updateRecurringTransaction(request.toCommand(id)))

    override suspend fun archive(id: String): RecurringTransactionResponse =
        RecurringTransactionResponse.from(archiveRecurringTransaction(id))

    override suspend fun unarchive(id: String): RecurringTransactionResponse =
        RecurringTransactionResponse.from(unarchiveRecurringTransaction(id))
}
