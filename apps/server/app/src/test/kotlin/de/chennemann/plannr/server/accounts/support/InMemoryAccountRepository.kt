package de.chennemann.plannr.server.accounts.support

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.persistence.AccountModel

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

    suspend fun save(account: Account): Account = save(
        AccountModel(
            id = account.id,
            name = account.name,
            institution = account.institution,
            currencyCode = account.currencyCode,
            weekendHandling = account.weekendHandling,
            isArchived = account.isArchived,
            createdAt = account.createdAt,
        ),
    )

    suspend fun update(account: Account): Account = update(
        AccountModel(
            id = account.id,
            name = account.name,
            institution = account.institution,
            currencyCode = account.currencyCode,
            weekendHandling = account.weekendHandling,
            isArchived = account.isArchived,
            createdAt = account.createdAt,
        ),
    )

    private fun AccountModel.withIdIfMissing(id: String): AccountModel = copy(id = this.id ?: id)

    private fun AccountModel.toDomain(): Account =
        Account(
            id = requireNotNull(id),
            name = name,
            institution = institution,
            currencyCode = currencyCode,
            weekendHandling = weekendHandling,
            isArchived = isArchived,
            createdAt = createdAt,
        )
}
