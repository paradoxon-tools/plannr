package de.chennemann.plannr.server.accounts.api

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountCommand
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountCommand
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/accounts")
interface AccountApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody command: CreateAccountCommand): Account

    @PutExchange
    suspend fun update(@RequestBody command: UpdateAccountCommand): Account

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: String): Account

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: String): Account

    @DeleteExchange("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: String)

    @GetExchange
    suspend fun list(
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<Account>

    @GetExchange("/{id}")
    suspend fun getById(@PathVariable id: String): Account
}
