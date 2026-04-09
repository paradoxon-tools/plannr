package de.chennemann.plannr.server.transactions.api

import de.chennemann.plannr.server.transactions.usecases.ArchiveTransaction
import de.chennemann.plannr.server.transactions.usecases.CreateTransaction
import de.chennemann.plannr.server.transactions.usecases.UnarchiveTransaction
import de.chennemann.plannr.server.transactions.usecases.UpdateTransaction
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transactions")
class TransactionIngressController(
    private val createTransaction: CreateTransaction,
    private val updateTransaction: UpdateTransaction,
    private val archiveTransaction: ArchiveTransaction,
    private val unarchiveTransaction: UnarchiveTransaction,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateTransactionRequest): TransactionResponse =
        TransactionResponse.from(createTransaction(request.toCommand()))

    @PutMapping("/{id}")
    suspend fun update(@PathVariable id: String, @RequestBody request: UpdateTransactionRequest): TransactionResponse =
        TransactionResponse.from(updateTransaction(request.toCommand(id)))

    @PostMapping("/{id}/archive")
    suspend fun archive(@PathVariable id: String): TransactionResponse =
        TransactionResponse.from(archiveTransaction(id))

    @PostMapping("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): TransactionResponse =
        TransactionResponse.from(unarchiveTransaction(id))
}
