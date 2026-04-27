package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.api.dto.ContractResponse
import de.chennemann.plannr.server.contracts.api.dto.CreateContractRequest
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractRequest
import de.chennemann.plannr.server.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketTransactionFeedPageResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/contracts")
interface ContractApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateContractRequest): ContractResponse

    @PutExchange("/{id}")
    suspend fun update(@PathVariable id: String, @RequestBody request: UpdateContractRequest): ContractResponse

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: String): ContractResponse

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): ContractResponse

    @GetExchange
    suspend fun list(
        @RequestParam(required = false) accountId: String?,
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<ContractResponse>

    @GetExchange("/{id}/transactions")
    suspend fun listTransactions(
        @PathVariable id: String,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(required = false) before: Long?,
    ): PocketTransactionFeedPageResponse

    @GetExchange("/{id}/future-transactions")
    suspend fun listFutureTransactions(
        @PathVariable id: String,
        @RequestParam(required = false) fromDate: String?,
        @RequestParam(required = false) toDate: String?,
        @RequestParam(required = false) after: Long?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): PocketFutureTransactionFeedPageResponse
}
