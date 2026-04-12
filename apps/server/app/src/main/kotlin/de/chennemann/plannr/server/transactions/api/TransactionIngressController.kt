package de.chennemann.plannr.server.transactions.api

import de.chennemann.plannr.server.transactions.dto.CreateTransactionRequest
import de.chennemann.plannr.server.transactions.dto.ModifyRecurringOccurrenceRequest
import de.chennemann.plannr.server.transactions.dto.TransactionResponse
import de.chennemann.plannr.server.transactions.dto.UpdateTransactionRequest
import de.chennemann.plannr.server.transactions.usecases.ArchiveTransaction
import de.chennemann.plannr.server.transactions.usecases.CreateTransaction
import de.chennemann.plannr.server.transactions.usecases.ModifyRecurringOccurrence
import de.chennemann.plannr.server.transactions.usecases.UnarchiveTransaction
import de.chennemann.plannr.server.transactions.usecases.UpdateTransaction
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionIngressController(
    private val createTransaction: CreateTransaction,
    private val updateTransaction: UpdateTransaction,
    private val modifyRecurringOccurrence: ModifyRecurringOccurrence,
    private val archiveTransaction: ArchiveTransaction,
    private val unarchiveTransaction: UnarchiveTransaction,
) : TransactionIngressApi {
    override suspend fun create(request: CreateTransactionRequest): TransactionResponse =
        TransactionResponse.from(createTransaction(request.toCommand()))

    override suspend fun update(id: String, request: UpdateTransactionRequest): TransactionResponse =
        TransactionResponse.from(updateTransaction(request.toCommand(id)))

    override suspend fun modifyRecurringOccurrence(id: String, request: ModifyRecurringOccurrenceRequest): TransactionResponse =
        TransactionResponse.from(modifyRecurringOccurrence(request.toCommand(id)))

    override suspend fun archive(id: String): TransactionResponse =
        TransactionResponse.from(archiveTransaction(id))

    override suspend fun unarchive(id: String): TransactionResponse =
        TransactionResponse.from(unarchiveTransaction(id))
}
