package de.chennemann.plannr.server.accounts.api

import de.chennemann.plannr.server.accounts.api.dto.AccountResponse
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountRequest
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountRequest
import de.chennemann.plannr.server.accounts.service.AccountService
import org.springframework.web.bind.annotation.RestController

@RestController
class AccountIngressController(
    private val accountService: AccountService,
) : AccountIngressApi {
    override suspend fun create(request: CreateAccountRequest): AccountResponse =
        accountService.create(request.toCommand()).toResponse()

    override suspend fun update(id: String, request: UpdateAccountRequest): AccountResponse =
        accountService.update(request.toCommand(id)).toResponse()

    override suspend fun archive(id: String): AccountResponse =
        accountService.archive(id).toResponse()

    override suspend fun unarchive(id: String): AccountResponse =
        accountService.unarchive(id).toResponse()
}
