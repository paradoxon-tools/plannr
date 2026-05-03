package de.chennemann.plannr.server.accounts.support

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.persistence.AccountModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.springframework.data.domain.Sort

class InMemoryAccountRepository : AccountRepository {
    private val accounts = linkedMapOf<String, AccountModel>()

    override suspend fun insert(
        id: String?,
        name: String,
        institution: String,
        currencyCode: String,
        weekendHandling: String,
        isArchived: Boolean,
        createdAt: Long,
    ): AccountModel =
        save(AccountModel(id, name, institution, currencyCode, weekendHandling, isArchived, createdAt))

    override suspend fun update(
        id: String,
        name: String,
        institution: String,
        currencyCode: String,
        weekendHandling: String,
        isArchived: Boolean,
    ): AccountModel =
        save(AccountModel(id, name, institution, currencyCode, weekendHandling, isArchived, accounts[id]?.createdAt ?: 0L, persisted = true))

    override suspend fun <S : AccountModel> save(entity: S): S {
        val persisted = entity.withIdIfMissing("acc_${accounts.size + 1}")
        accounts[requireNotNull(persisted.id)] = persisted
        @Suppress("UNCHECKED_CAST")
        return persisted as S
    }

    override fun findAllByOrderByCreatedAtAscIdAsc(): Flow<AccountModel> =
        accounts.values.sortedWith(compareBy<AccountModel> { it.createdAt }.thenBy { requireNotNull(it.id) }).asFlow()

    override suspend fun findById(id: String): AccountModel? = accounts[id]

    override suspend fun existsById(id: String): Boolean = accounts.containsKey(id)

    override fun findAll(): Flow<AccountModel> = accounts.values.asFlow()

    override fun findAll(sort: Sort): Flow<AccountModel> = findAllByOrderByCreatedAtAscIdAsc()

    override fun findAllById(ids: Iterable<String>): Flow<AccountModel> =
        ids.mapNotNull(accounts::get).asFlow()

    override fun findAllById(ids: Flow<String>): Flow<AccountModel> = flow {
        ids.collect { id -> accounts[id]?.let { emit(it) } }
    }

    override fun <S : AccountModel> saveAll(entities: Iterable<S>): Flow<S> = flow {
        entities.forEach { emit(save(it)) }
    }

    override fun <S : AccountModel> saveAll(entityStream: Flow<S>): Flow<S> = flow {
        entityStream.collect { emit(save(it)) }
    }

    override suspend fun count(): Long = accounts.size.toLong()

    override suspend fun deleteById(id: String) {
        accounts.remove(id)
    }

    override suspend fun delete(entity: AccountModel) {
        entity.id?.let(accounts::remove)
    }

    override suspend fun deleteAllById(ids: Iterable<String>) {
        ids.forEach(accounts::remove)
    }

    override suspend fun deleteAll(entities: Iterable<AccountModel>) {
        entities.mapNotNull { it.id }.forEach(accounts::remove)
    }

    override suspend fun <S : AccountModel> deleteAll(entityStream: Flow<S>) {
        entityStream.collect { delete(it) }
    }

    override suspend fun deleteAll() {
        accounts.clear()
    }

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
    ).toDomain()

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
