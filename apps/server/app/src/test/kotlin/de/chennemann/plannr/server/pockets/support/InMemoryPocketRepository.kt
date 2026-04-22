package de.chennemann.plannr.server.pockets.support

import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketRepository

class InMemoryPocketRepository : PocketRepository {
    private val pockets = linkedMapOf<String, Pocket>()

    override suspend fun save(pocket: Pocket): Pocket {
        pockets[pocket.id] = pocket
        return pocket
    }

    override suspend fun update(pocket: Pocket): Pocket {
        pockets[pocket.id] = pocket
        return pocket
    }

    override suspend fun findById(id: String): Pocket? =
        pockets[id]

    override suspend fun findAll(accountId: String?, archived: Boolean?): List<Pocket> =
        pockets.values
            .filter { accountId == null || it.accountId == accountId }
            .filter { archived == null || it.isArchived == archived }
            .sortedWith(compareBy<Pocket> { it.createdAt }.thenBy { it.id })
}
