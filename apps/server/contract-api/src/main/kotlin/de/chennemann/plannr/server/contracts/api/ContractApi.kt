package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.api.dto.Contract
import de.chennemann.plannr.server.contracts.api.dto.CreateContractCommand
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractCommand
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
    suspend fun create(@RequestBody command: CreateContractCommand): Contract

    @PutExchange
    suspend fun update(@RequestBody command: UpdateContractCommand): Contract

    @GetExchange
    suspend fun list(
        @RequestParam(required = false) accountId: Long?,
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<Contract>

    @GetExchange("/{id}")
    suspend fun getById(@PathVariable id: Long): Contract
}
