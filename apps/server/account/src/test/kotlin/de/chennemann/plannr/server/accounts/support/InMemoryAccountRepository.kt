package de.chennemann.plannr.server.accounts.support

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.persistence.AccountModel
import de.chennemann.plannr.server.accounts.persistence.toDTO
import de.chennemann.plannr.server.accounts.persistence.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow

class InMemoryAccountRepository : AccountRepository {
    private val accounts = linkedMapOf<Long, AccountModel>()

    override suspend fun <S : AccountModel> save(entity: S): S {
        val persisted = entity.copy(id = entity.id ?: (accounts.size + 1).toLong())
        accounts[requireNotNull(persisted.id)] = persisted
        @Suppress("UNCHECKED_CAST")
        return persisted as S
    }

    override suspend fun findById(id: Long): AccountModel? = accounts[id]

    override suspend fun findByNameAndInstitution(name: String, institution: String): AccountModel? =
        accounts.values.firstOrNull { it.name == name && it.institution == institution }

    override fun findAllByOrderByCreatedAtAscIdAsc(): Flow<AccountModel> =
        accounts.values.sortedWith(compareBy<AccountModel> { it.createdAt }.thenBy { requireNotNull(it.id) }).asFlow()

    override suspend fun existsById(id: Long): Boolean = accounts.containsKey(id)

    override fun findAll(): Flow<AccountModel> = accounts.values.asFlow()

    override fun findAllById(ids: Iterable<Long>): Flow<AccountModel> =
        ids.mapNotNull(accounts::get).asFlow()

    override fun findAllById(ids: Flow<Long>): Flow<AccountModel> = flow {
        ids.collect { id -> accounts[id]?.let { emit(it) } }
    }

    override fun <S : AccountModel> saveAll(entities: Iterable<S>): Flow<S> = flow {
        entities.forEach { emit(save(it)) }
    }

    override fun <S : AccountModel> saveAll(entityStream: Flow<S>): Flow<S> = flow {
        entityStream.collect { emit(save(it)) }
    }

    override suspend fun count(): Long = accounts.size.toLong()

    override suspend fun deleteById(id: Long) {
        accounts.remove(id)
    }

    override suspend fun delete(entity: AccountModel) {
        entity.id?.let(accounts::remove)
    }

    override suspend fun deleteAllById(ids: Iterable<Long>) {
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

    suspend fun save(account: Account): Account = save(account.toModel()).toDTO()
}
