package de.chennemann.plannr.server.accounts.api

import de.chennemann.plannr.server.accounts.dto.AccountResponse
import de.chennemann.plannr.server.accounts.dto.CreateAccountRequest
import de.chennemann.plannr.server.accounts.dto.UpdateAccountRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/accounts")
interface AccountIngressApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateAccountRequest): AccountResponse

    @PutExchange("/{id}")
    suspend fun update(@PathVariable id: String, @RequestBody request: UpdateAccountRequest): AccountResponse

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: String): AccountResponse

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): AccountResponse
}
