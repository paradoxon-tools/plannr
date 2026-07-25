package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.ContractModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class InMemoryContractRepository : ContractRepository {
    private val contracts = linkedMapOf<Long, ContractModel>()

    override suspend fun <S : ContractModel> save(entity: S): S {
        val persisted = entity.copy(id = entity.id ?: ((contracts.keys.maxOrNull() ?: 0) + 1))
        contracts[requireNotNull(persisted.id)] = persisted
        @Suppress("UNCHECKED_CAST")
        return persisted as S
    }

    override fun findAllByAccountIdAndArchived(accountId: Long?, archived: Boolean): Flow<ContractModel> =
        contracts.values.filter { it.isArchived == archived }.asFlow()

    override suspend fun findById(id: Long) = contracts[id]
    override suspend fun existsById(id: Long) = contracts.containsKey(id)
    override fun findAll(): Flow<ContractModel> = contracts.values.asFlow()
    override fun findAllById(ids: Iterable<Long>): Flow<ContractModel> = ids.mapNotNull(contracts::get).asFlow()
    override fun findAllById(ids: Flow<Long>): Flow<ContractModel> = flow { ids.collect { contracts[it]?.let { model -> emit(model) } } }
    override fun <S : ContractModel> saveAll(entities: Iterable<S>): Flow<S> = flow { entities.forEach { emit(save(it)) } }
    override fun <S : ContractModel> saveAll(entityStream: Flow<S>): Flow<S> = flow { entityStream.collect { emit(save(it)) } }
    override suspend fun count() = contracts.size.toLong()
    override suspend fun deleteById(id: Long) { contracts.remove(id) }
    override suspend fun delete(entity: ContractModel) { entity.id?.let(contracts::remove) }
    override suspend fun deleteAllById(ids: Iterable<Long>) { ids.forEach(contracts::remove) }
    override suspend fun deleteAll(entities: Iterable<ContractModel>) { entities.mapNotNull { it.id }.forEach(contracts::remove) }
    override suspend fun <S : ContractModel> deleteAll(entityStream: Flow<S>) { entityStream.collect { delete(it) } }
    override suspend fun deleteAll() { contracts.clear() }
}
