package de.chennemann.plannr.server.accounts.support

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.persistence.AccountModel
import de.chennemann.plannr.server.accounts.persistence.toDomain

class InMemoryAccountRepository : AccountRepository {
    private val accounts = linkedMapOf<String, Account>()

    override suspend fun save(account: AccountModel): Account {
        val persisted = account.withIdIfMissing("acc_${accounts.size + 1}").toDomain()
        accounts[persisted.id] = persisted
        return persisted
    }

    override suspend fun update(account: AccountModel): Account {
        val persisted = account.withIdIfMissing("acc_${accounts.size + 1}").toDomain()
        accounts[persisted.id] = persisted
        return persisted
    }

    override suspend fun findById(id: String): Account? = accounts[id]

    override suspend fun findAll(): List<Account> = accounts.values.toList()

    private fun AccountModel.withIdIfMissing(id: String): AccountModel = copy(id = this.id ?: id)
}
