package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.ContractModel
import de.chennemann.plannr.server.contracts.persistence.ContractPocketRow
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class InMemoryContractRepository(
    private val pockets: () -> Collection<Pocket>,
) : ContractRepository {
    private val contracts = linkedMapOf<Long, ContractModel>()

    override suspend fun <S : ContractModel> save(entity: S): S {
        contracts[entity.pocketId] = entity
        return entity
    }

    override suspend fun findById(id: Long): ContractModel? = contracts[id]

    override suspend fun upsert(
        pocketId: Long,
        partnerId: Long?,
        signingDate: String?,
        expirationDate: String?,
        lastCancellationDate: String?,
    ): Int {
        contracts[pocketId] = ContractModel(pocketId, partnerId, signingDate, expirationDate, lastCancellationDate)
        return 1
    }

    override fun findAllWithPocketsByAccountIdAndArchived(
        accountId: Long?,
        archived: Boolean,
    ): Flow<ContractPocketRow> =
        contracts.keys
            .mapNotNull(::joinPocketAndContract)
            .filter { it.isArchived == archived && (accountId == null || it.accountId == accountId) }
            .sortedWith(compareBy<ContractPocketRow> { it.createdAt }.thenBy { it.id })
            .asFlow()

    override suspend fun findWithPocketByPocketId(pocketId: Long): ContractPocketRow? =
        joinPocketAndContract(pocketId)

    private fun joinPocketAndContract(pocketId: Long): ContractPocketRow? {
        val pocket = pockets().firstOrNull { it.id == pocketId } ?: return null
        val contract = contracts[pocketId] ?: return null
        return ContractPocketRow(
            id = pocket.id,
            accountId = pocket.accountId,
            name = pocket.name,
            description = pocket.description,
            color = pocket.color,
            isDefault = pocket.isDefault,
            isContractPocket = pocket.isContractPocket,
            isArchived = pocket.isArchived,
            createdAt = pocket.createdAt,
            partnerId = contract.partnerId,
            signingDate = contract.signingDate,
            expirationDate = contract.expirationDate,
            lastCancellationDate = contract.lastCancellationDate,
        )
    }

    override suspend fun existsById(id: Long): Boolean = contracts.containsKey(id)
    override fun findAll(): Flow<ContractModel> = contracts.values.asFlow()
    override fun findAllById(ids: Iterable<Long>): Flow<ContractModel> = ids.mapNotNull(contracts::get).asFlow()
    override fun findAllById(ids: Flow<Long>): Flow<ContractModel> = flow { ids.collect { id -> contracts[id]?.let { emit(it) } } }
    override fun <S : ContractModel> saveAll(entities: Iterable<S>): Flow<S> = flow { entities.forEach { emit(save(it)) } }
    override fun <S : ContractModel> saveAll(entityStream: Flow<S>): Flow<S> = flow { entityStream.collect { emit(save(it)) } }
    override suspend fun count(): Long = contracts.size.toLong()
    override suspend fun deleteById(id: Long) {
        contracts.remove(id)
    }
    override suspend fun delete(entity: ContractModel) {
        contracts.remove(entity.pocketId)
    }
    override suspend fun deleteAllById(ids: Iterable<Long>) {
        ids.forEach(contracts::remove)
    }
    override suspend fun deleteAll(entities: Iterable<ContractModel>) {
        entities.map { it.pocketId }.forEach(contracts::remove)
    }
    override suspend fun <S : ContractModel> deleteAll(entityStream: Flow<S>) {
        entityStream.collect { delete(it) }
    }
    override suspend fun deleteAll() {
        contracts.clear()
    }
}
