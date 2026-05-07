package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.ContractModel
import de.chennemann.plannr.server.contracts.persistence.PocketWithContractModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class InMemoryContractRepository : ContractRepository {
    private val contracts = linkedMapOf<Long, ContractModel>()
    private val pockets = linkedMapOf(
        ContractFixtures.DEFAULT_POCKET_ID to ContractTestPockets.pocket(),
        2L to ContractTestPockets.pocket(id = 2L, accountId = 2L, name = "Rent", isArchived = true),
    )

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

    override fun findAllWithPocketsByAccountIdAndArchived(accountId: Long?, archived: Boolean): Flow<PocketWithContractModel> =
        contracts.values
            .mapNotNull { contract ->
                val pocket = pockets[contract.pocketId] ?: return@mapNotNull null
                if (pocket.isArchived != archived || (accountId != null && pocket.accountId != accountId)) {
                    return@mapNotNull null
                }
                PocketWithContractModel(
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
            .sortedWith(compareBy<PocketWithContractModel> { it.createdAt }.thenBy { it.id })
            .asFlow()

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
