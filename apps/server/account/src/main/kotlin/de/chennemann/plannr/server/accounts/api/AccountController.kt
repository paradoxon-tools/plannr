package de.chennemann.plannr.server.accounts.api

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountRequest
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountRequest
import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.common.error.NotFoundException
import org.springframework.web.bind.annotation.RestController

@RestController
class AccountController(
    private val accountService: AccountService,
) : AccountApi {
    override suspend fun create(request: CreateAccountRequest): Account =
        accountService.create(request.toCommand())

    override suspend fun update(id: String, request: UpdateAccountRequest): Account =
        accountService.update(request.toCommand(id))

    override suspend fun archive(id: String): Account =
        accountService.archive(id)

    override suspend fun unarchive(id: String): Account =
        accountService.unarchive(id)

    override suspend fun list(archived: Boolean): List<Account> =
        accountService.list(archived)

    override suspend fun getById(id: String): Account =
        accountService.getById(id.trim())
            ?: throw NotFoundException("not_found", "Account not found", mapOf("id" to id.trim()))
}
