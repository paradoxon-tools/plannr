package de.chennemann.plannr.server.recurringtransactions.api

import de.chennemann.plannr.server.recurringtransactions.usecases.ArchiveRecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.usecases.CreateRecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.usecases.UnarchiveRecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.usecases.UpdateRecurringTransaction
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/recurring-transactions")
class RecurringTransactionIngressController(
    private val createRecurringTransaction: CreateRecurringTransaction,
    private val updateRecurringTransaction: UpdateRecurringTransaction,
    private val archiveRecurringTransaction: ArchiveRecurringTransaction,
    private val unarchiveRecurringTransaction: UnarchiveRecurringTransaction,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateRecurringTransactionRequest): RecurringTransactionResponse =
        RecurringTransactionResponse.from(createRecurringTransaction(request.toCommand()))


    @PutMapping("/{id}")
    suspend fun update(@PathVariable id: String, @RequestBody request: UpdateRecurringTransactionRequest): RecurringTransactionResponse =
        RecurringTransactionResponse.from(updateRecurringTransaction(request.toCommand(id)))

    @PostMapping("/{id}/archive")
    suspend fun archive(@PathVariable id: String): RecurringTransactionResponse =
        RecurringTransactionResponse.from(archiveRecurringTransaction(id))

    @PostMapping("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): RecurringTransactionResponse =
        RecurringTransactionResponse.from(unarchiveRecurringTransaction(id))
}
