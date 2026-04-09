package de.chennemann.plannr.server.accounts.support

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository

class InMemoryAccountRepository : AccountRepository {
    private val accounts = linkedMapOf<String, Account>()

    override suspend fun save(account: Account): Account {
        accounts[account.id] = account
        return account
    }

    override suspend fun update(account: Account): Account {
        accounts[account.id] = account
        return account
    }

    override suspend fun findById(id: String): Account? = accounts[id]
}
