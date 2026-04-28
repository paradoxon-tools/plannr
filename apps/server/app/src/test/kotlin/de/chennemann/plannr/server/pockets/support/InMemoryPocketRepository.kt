package de.chennemann.plannr.server.pockets.support

import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.persistence.PocketModel

class InMemoryPocketRepository : PocketRepository {
    private val pockets = linkedMapOf<String, Pocket>()

    override suspend fun save(pocket: PocketModel): Pocket {
        val persisted = pocket.withIdIfMissing("poc_${pockets.size + 1}").toDomain()
        pockets[persisted.id] = persisted
        return persisted
    }

    override suspend fun update(pocket: PocketModel): Pocket {
        val persisted = pocket.withIdIfMissing("poc_${pockets.size + 1}").toDomain()
        pockets[persisted.id] = persisted
        return persisted
    }

    override suspend fun findById(id: String): Pocket? =
        pockets[id]

    override suspend fun findAll(accountId: String?, archived: Boolean?): List<Pocket> =
        pockets.values
            .filter { accountId == null || it.accountId == accountId }
            .filter { archived == null || it.isArchived == archived }
            .sortedWith(compareBy<Pocket> { it.createdAt }.thenBy { it.id })

    suspend fun save(pocket: Pocket): Pocket = save(
        PocketModel(
            id = pocket.id,
            accountId = pocket.accountId,
            name = pocket.name,
            description = pocket.description,
            color = pocket.color,
            isDefault = pocket.isDefault,
            isArchived = pocket.isArchived,
            createdAt = pocket.createdAt,
        ),
    )

    suspend fun update(pocket: Pocket): Pocket = update(
        PocketModel(
            id = pocket.id,
            accountId = pocket.accountId,
            name = pocket.name,
            description = pocket.description,
            color = pocket.color,
            isDefault = pocket.isDefault,
            isArchived = pocket.isArchived,
            createdAt = pocket.createdAt,
        ),
    )

    private fun PocketModel.withIdIfMissing(id: String): PocketModel = copy(id = this.id ?: id)

    private fun PocketModel.toDomain(): Pocket =
        Pocket(
            id = requireNotNull(id),
            accountId = accountId,
            name = name,
            description = description,
            color = color,
            isDefault = isDefault,
            isArchived = isArchived,
            createdAt = createdAt,
        )
}
