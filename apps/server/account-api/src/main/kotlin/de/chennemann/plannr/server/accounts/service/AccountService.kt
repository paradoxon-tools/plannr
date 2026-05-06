package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountCommand
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountCommand

interface AccountService {
    suspend fun create(command: CreateAccountCommand): Account
    suspend fun update(command: UpdateAccountCommand): Account
    suspend fun archive(id: Long): Account
    suspend fun unarchive(id: Long): Account
    suspend fun delete(id: Long)
    suspend fun list(archived: Boolean? = null): List<Account>
    suspend fun getById(id: Long): Account?
}
