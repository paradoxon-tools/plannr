package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.dto.ContractResponse
import de.chennemann.plannr.server.contracts.dto.CreateContractRequest
import de.chennemann.plannr.server.contracts.dto.UpdateContractRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/contracts")
interface ContractIngressApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateContractRequest): ContractResponse

    @PutExchange("/{id}")
    suspend fun update(@PathVariable id: String, @RequestBody request: UpdateContractRequest): ContractResponse

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: String): ContractResponse

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): ContractResponse
}
