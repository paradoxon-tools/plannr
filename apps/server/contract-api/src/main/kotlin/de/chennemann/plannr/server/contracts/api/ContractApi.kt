package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.api.dto.Contract
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange("/contracts")
interface ContractApi {
    @DeleteExchange("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: Long)

    @GetExchange
    suspend fun list(
        @RequestParam(required = false) accountId: Long?,
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<Contract>
}
