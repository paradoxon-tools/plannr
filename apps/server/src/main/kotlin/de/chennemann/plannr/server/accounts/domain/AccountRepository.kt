package de.chennemann.plannr.server.accounts.domain

interface AccountRepository {
    suspend fun save(account: Account): Account
    suspend fun update(account: Account): Account
    suspend fun findById(id: String): Account?
}
