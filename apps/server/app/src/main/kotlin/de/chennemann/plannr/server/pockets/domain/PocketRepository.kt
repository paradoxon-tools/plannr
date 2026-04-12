package de.chennemann.plannr.server.pockets.domain

interface PocketRepository {
    suspend fun save(pocket: Pocket): Pocket
    suspend fun update(pocket: Pocket): Pocket
    suspend fun findById(id: String): Pocket?
    suspend fun findAll(accountId: String? = null, archived: Boolean? = null): List<Pocket>
}
