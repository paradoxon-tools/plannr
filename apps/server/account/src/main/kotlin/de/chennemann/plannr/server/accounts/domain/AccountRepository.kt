package de.chennemann.plannr.server.accounts.domain

import de.chennemann.plannr.server.accounts.persistence.AccountModel

interface AccountRepository {
    suspend fun save(account: AccountModel): Account
    suspend fun update(account: AccountModel): Account
    suspend fun findById(id: String): Account?
    suspend fun findAll(): List<Account>
}
