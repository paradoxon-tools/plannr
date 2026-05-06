package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.ContractModel
import de.chennemann.plannr.server.contracts.persistence.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class InMemoryContractRepository : ContractRepository {
    private val contracts = linkedMapOf<String, ContractModel>()

    override suspend fun insert(
        id: String?,
        accountId: Long,
        pocketId: String,
        partnerId: String?,
        name: String,
        startDate: String,
        endDate: String?,
        notes: String?,
        isArchived: Boolean,
        createdAt: Long,
    ): ContractModel =
        save(ContractModel(id, accountId, pocketId, partnerId, name, startDate, endDate, notes, isArchived, createdAt))

    override suspend fun update(
        id: String,
        accountId: Long,
        pocketId: String,
        partnerId: String?,
        name: String,
        startDate: String,
        endDate: String?,
        notes: String?,
        isArchived: Boolean,
    ): ContractModel =
        save(ContractModel(id, accountId, pocketId, partnerId, name, startDate, endDate, notes, isArchived, contracts[id]?.createdAt ?: 0L, persisted = true))

    override suspend fun <S : ContractModel> save(entity: S): S {
        val persisted = entity.withIdIfMissing("con_${contracts.size + 1}")
        contracts[requireNotNull(persisted.id)] = persisted
        @Suppress("UNCHECKED_CAST")
        return persisted as S
    }

    override suspend fun findById(id: String): ContractModel? = contracts[id]

    override suspend fun findByPocketId(pocketId: String): ContractModel? =
        contracts.values.firstOrNull { it.pocketId == pocketId }

    override fun findAllByAccountIdAndArchived(accountId: Long?, archived: Boolean): Flow<ContractModel> =
        contracts.values
            .filter { it.isArchived == archived }
            .filter { accountId == null || it.accountId == accountId }
            .sortedWith(compareBy<ContractModel> { it.createdAt }.thenBy { requireNotNull(it.id) })
            .asFlow()

    override suspend fun existsById(id: String): Boolean = contracts.containsKey(id)

    override fun findAll(): Flow<ContractModel> = contracts.values.asFlow()

    override fun findAllById(ids: Iterable<String>): Flow<ContractModel> =
        ids.mapNotNull(contracts::get).asFlow()

    override fun findAllById(ids: Flow<String>): Flow<ContractModel> = flow {
        ids.collect { id -> contracts[id]?.let { emit(it) } }
    }

    override fun <S : ContractModel> saveAll(entities: Iterable<S>): Flow<S> = flow {
        entities.forEach { emit(save(it)) }
    }

    override fun <S : ContractModel> saveAll(entityStream: Flow<S>): Flow<S> = flow {
        entityStream.collect { emit(save(it)) }
    }

    override suspend fun count(): Long = contracts.size.toLong()

    override suspend fun deleteById(id: String) { contracts.remove(id) }

    override suspend fun delete(entity: ContractModel) {
        entity.id?.let(contracts::remove)
    }

    override suspend fun deleteAllById(ids: Iterable<String>) { ids.forEach(contracts::remove) }

    override suspend fun deleteAll(entities: Iterable<ContractModel>) {
        entities.mapNotNull { it.id }.forEach(contracts::remove)
    }

    override suspend fun <S : ContractModel> deleteAll(entityStream: Flow<S>) {
        entityStream.collect { delete(it) }
    }

    override suspend fun deleteAll() {
        contracts.clear()
    }

    suspend fun save(contract: Contract): Contract = save(contract.toModel()).toDomain()

    suspend fun update(contract: Contract): Contract = save(contract)

    fun peekByPocketId(pocketId: String): Contract? =
        contracts.values.firstOrNull { it.pocketId == pocketId }?.toDomain()

    private fun ContractModel.withIdIfMissing(id: String): ContractModel = copy(id = this.id ?: id)

    private fun ContractModel.toDomain(): Contract =
        Contract(
            id = requireNotNull(id),
            accountId = accountId,
            pocketId = pocketId,
            partnerId = partnerId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
            isArchived = isArchived,
            createdAt = createdAt,
        )
}
