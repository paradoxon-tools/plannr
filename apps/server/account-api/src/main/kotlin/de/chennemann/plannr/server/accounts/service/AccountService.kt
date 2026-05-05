package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountCommand
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountCommand

interface AccountService {
    suspend fun create(command: CreateAccountCommand): Account
    suspend fun update(command: UpdateAccountCommand): Account
    suspend fun archive(id: String): Account
    suspend fun unarchive(id: String): Account
    suspend fun delete(id: String)
    suspend fun list(archived: Boolean? = null): List<Account>
    suspend fun getById(id: String): Account?
}
