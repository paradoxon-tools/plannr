package de.chennemann.plannr.server.pockets.support

import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository
import de.chennemann.plannr.server.pockets.persistence.PocketModel
import de.chennemann.plannr.server.pockets.persistence.toDomain

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

    override suspend fun findById(id: String): Pocket? = pockets[id]

    override suspend fun findAll(accountId: String?, archived: Boolean?): List<Pocket> =
        pockets.values.filter { pocket ->
            (accountId == null || pocket.accountId == accountId) &&
                (archived == null || pocket.isArchived == archived)
        }

    private fun PocketModel.withIdIfMissing(id: String): PocketModel = copy(id = this.id ?: id)
}
