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
    private val pockets = linkedMapOf<String, PocketModel>()

    override suspend fun insert(
        id: String?,
        accountId: Long,
        name: String,
        description: String?,
        color: Int,
        isDefault: Boolean,
        isContractPocket: Boolean,
        isArchived: Boolean,
        createdAt: Long,
    ): PocketModel =
        save(PocketModel(id, accountId, name, description, color, isDefault, isContractPocket, isArchived, createdAt))

    override suspend fun update(
        id: String,
        accountId: Long,
        name: String,
        description: String?,
        color: Int,
        isDefault: Boolean,
        isArchived: Boolean,
    ): PocketModel =
        save(
            PocketModel(
                id = id,
                accountId = accountId,
                name = name,
                description = description,
                color = color,
                isDefault = isDefault,
                isContractPocket = pockets[id]?.isContractPocket ?: false,
                isArchived = isArchived,
                createdAt = pockets[id]?.createdAt ?: 0L,
                persisted = true,
            ),
        )

    override suspend fun <S : PocketModel> save(entity: S): S {
        val persisted = entity.withIdIfMissing("poc_${pockets.size + 1}")
        pockets[requireNotNull(persisted.id)] = persisted
        @Suppress("UNCHECKED_CAST")
        return persisted as S
    }

    override suspend fun findById(id: String): PocketModel? = pockets[id]

    override suspend fun existsById(id: String): Boolean = pockets.containsKey(id)

    override fun findAll(): Flow<PocketModel> = pockets.values.asFlow()

    override fun findAllById(ids: Iterable<String>): Flow<PocketModel> =
        ids.mapNotNull(pockets::get).asFlow()

    override fun findAllById(ids: Flow<String>): Flow<PocketModel> = flow {
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
            .asFlow()

    override suspend fun count(): Long = pockets.size.toLong()

    override suspend fun deleteById(id: String) { pockets.remove(id) }

    override suspend fun delete(entity: PocketModel) {
        entity.id?.let(pockets::remove)
    }

    override suspend fun deleteAllById(ids: Iterable<String>) { ids.forEach(pockets::remove) }

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

    private fun PocketModel.withIdIfMissing(id: String): PocketModel = copy(id = this.id ?: id)

    private fun PocketModel.toDomain(): Pocket =
        Pocket(
            id = requireNotNull(id),
            accountId = accountId,
            name = name,
            description = description,
            color = color,
            isDefault = isDefault,
            isContractPocket = isContractPocket,
            isArchived = isArchived,
            createdAt = createdAt,
        )
}
