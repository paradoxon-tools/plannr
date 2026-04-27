package de.chennemann.plannr.server.pockets.domain

import de.chennemann.plannr.server.pockets.persistence.PocketModel

interface PocketRepository {
    suspend fun save(pocket: PocketModel): Pocket
    suspend fun update(pocket: PocketModel): Pocket
    suspend fun findById(id: String): Pocket?
    suspend fun findAll(accountId: String? = null, archived: Boolean? = null): List<Pocket>
}
