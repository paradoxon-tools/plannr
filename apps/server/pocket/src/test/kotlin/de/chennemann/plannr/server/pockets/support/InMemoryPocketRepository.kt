package de.chennemann.plannr.server.pockets.support

import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.persistence.PocketModel
import de.chennemann.plannr.server.pockets.persistence.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class InMemoryPocketRepository : PocketRepository {
    private val pockets = linkedMapOf<Long, PocketModel>()

    override suspend fun <S : PocketModel> save(entity: S): S {
        val persisted = entity.withIdIfMissing((pockets.size + 1).toLong())
        pockets[requireNotNull(persisted.id)] = persisted
        @Suppress("UNCHECKED_CAST")
        return persisted as S
    }

    override suspend fun findById(id: Long): PocketModel? = pockets[id]

    override suspend fun existsById(id: Long): Boolean = pockets.containsKey(id)

    override fun findAll(): Flow<PocketModel> = pockets.values.asFlow()

    override fun findAllById(ids: Iterable<Long>): Flow<PocketModel> =
        ids.mapNotNull(pockets::get).asFlow()

    override fun findAllById(ids: Flow<Long>): Flow<PocketModel> = flow {
        ids.collect { id -> pockets[id]?.let { emit(it) } }
    }

    override fun <S : PocketModel> saveAll(entities: Iterable<S>): Flow<S> = flow {
        entities.forEach { emit(save(it)) }
    }

    override fun <S : PocketModel> saveAll(entityStream: Flow<S>): Flow<S> = flow {
        entityStream.collect { emit(save(it)) }
    }

    override fun findAllByAccountIdAndArchived(accountId: Long?, archived: Boolean?): Flow<PocketModel> =
        pockets.values
            .filter { accountId == null || it.accountId == accountId }
            .filter { archived == null || it.isArchived == archived }
            .sortedWith(compareBy<PocketModel> { it.createdAt }.thenBy { requireNotNull(it.id) })
            .map { it.toResolvedModel() }
            .asFlow()

    override fun findAllBySavingGoalId(savingGoalId: Long): Flow<PocketModel> =
        pockets.values
            .filter { it.savingGoalId == savingGoalId }
            .sortedWith(compareBy<PocketModel> { it.createdAt }.thenBy { requireNotNull(it.id) })
            .map { it.toResolvedModel() }
            .asFlow()

    override suspend fun findDefaultByAccountId(accountId: Long): PocketModel? =
        pockets.values
            .filter { it.accountId == accountId && it.isDefault }
            .minWithOrNull(compareBy<PocketModel> { it.createdAt }.thenBy { requireNotNull(it.id) })
            ?.toResolvedModel()

    override suspend fun findResolvedById(id: Long): PocketModel? = pockets[id]?.toResolvedModel()

    override suspend fun count(): Long = pockets.size.toLong()

    override suspend fun deleteById(id: Long) { pockets.remove(id) }

    override suspend fun delete(entity: PocketModel) {
        entity.id?.let(pockets::remove)
    }

    override suspend fun deleteAllById(ids: Iterable<Long>) { ids.forEach(pockets::remove) }

    override suspend fun deleteAll(entities: Iterable<PocketModel>) {
        entities.mapNotNull { it.id }.forEach(pockets::remove)
    }

    override suspend fun <S : PocketModel> deleteAll(entityStream: Flow<S>) {
        entityStream.collect { delete(it) }
    }

    override suspend fun deleteAll() {
        pockets.clear()
    }

    suspend fun save(pocket: Pocket): Pocket = save(pocket.toModel()).toDomain()

    private fun PocketModel.withIdIfMissing(id: Long): PocketModel = copy(id = this.id ?: id)

    private fun PocketModel.toDomain(): Pocket =
        Pocket(
            id = requireNotNull(id),
            accountId = accountId,
            contractId = contractId,
            savingGoalId = savingGoalId,
            name = name ?: "Contract $contractId",
            description = description,
            color = color ?: 0,
            isDefault = isDefault,
            isArchived = isArchived,
            createdAt = createdAt,
        )

    // Mirrors the production repository's contract join for tests that do not
    // provide a contract repository.
    private fun PocketModel.toResolvedModel(): PocketModel =
        copy(
            name = name ?: "Contract $contractId",
            color = color ?: 0,
        )
}
