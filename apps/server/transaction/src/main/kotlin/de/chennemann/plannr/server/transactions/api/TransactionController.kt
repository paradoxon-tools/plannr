package de.chennemann.plannr.server.transactions.api

import de.chennemann.plannr.server.transactions.api.dto.CreateTransactionRequest
import de.chennemann.plannr.server.transactions.api.dto.ModifyRecurringOccurrenceRequest
import de.chennemann.plannr.server.transactions.api.dto.TransactionResponse
import de.chennemann.plannr.server.transactions.api.dto.UpdateTransactionRequest
import de.chennemann.plannr.server.transactions.usecases.ArchiveTransaction
import de.chennemann.plannr.server.transactions.usecases.CreateTransaction
import de.chennemann.plannr.server.transactions.usecases.ListTransactionsQuery
import de.chennemann.plannr.server.transactions.usecases.ModifyRecurringOccurrence
import de.chennemann.plannr.server.transactions.usecases.UnarchiveTransaction
import de.chennemann.plannr.server.transactions.usecases.UpdateTransaction
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionController(
    private val createTransaction: CreateTransaction,
    private val updateTransaction: UpdateTransaction,
    private val modifyRecurringOccurrence: ModifyRecurringOccurrence,
    private val archiveTransaction: ArchiveTransaction,
    private val unarchiveTransaction: UnarchiveTransaction,
    private val listTransactionsQuery: ListTransactionsQuery,
) : TransactionApi {
    override suspend fun create(request: CreateTransactionRequest): TransactionResponse =
        createTransaction(request.toCommand()).toResponse()

    override suspend fun update(id: String, request: UpdateTransactionRequest): TransactionResponse =
        updateTransaction(request.toCommand(id)).toResponse()

    override suspend fun modifyRecurringOccurrence(id: String, request: ModifyRecurringOccurrenceRequest): TransactionResponse =
        modifyRecurringOccurrence(request.toCommand(id)).toResponse()

    override suspend fun archive(id: String): TransactionResponse =
        archiveTransaction(id).toResponse()

    override suspend fun unarchive(id: String): TransactionResponse =
        unarchiveTransaction(id).toResponse()

    override suspend fun list(accountId: String?, pocketId: String?, archived: Boolean): List<TransactionResponse> =
        listTransactionsQuery(accountId, pocketId, archived).map { it.toResponse() }
}
