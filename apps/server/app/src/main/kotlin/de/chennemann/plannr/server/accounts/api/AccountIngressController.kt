package de.chennemann.plannr.server.accounts.api

import de.chennemann.plannr.server.accounts.api.dto.AccountResponse
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountRequest
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountRequest
import de.chennemann.plannr.server.accounts.usecases.ArchiveAccount
import de.chennemann.plannr.server.accounts.usecases.CreateAccount
import de.chennemann.plannr.server.accounts.usecases.UnarchiveAccount
import de.chennemann.plannr.server.accounts.usecases.UpdateAccount
import org.springframework.web.bind.annotation.RestController

@RestController
class AccountIngressController(
    private val createAccount: CreateAccount,
    private val updateAccount: UpdateAccount,
    private val archiveAccount: ArchiveAccount,
    private val unarchiveAccount: UnarchiveAccount,
) : AccountIngressApi {
    override suspend fun create(request: CreateAccountRequest): AccountResponse =
        createAccount(request.toCommand()).toResponse()

    override suspend fun update(id: String, request: UpdateAccountRequest): AccountResponse =
        updateAccount(request.toCommand(id)).toResponse()

    override suspend fun archive(id: String): AccountResponse =
        archiveAccount(id).toResponse()

    override suspend fun unarchive(id: String): AccountResponse =
        unarchiveAccount(id).toResponse()
}
