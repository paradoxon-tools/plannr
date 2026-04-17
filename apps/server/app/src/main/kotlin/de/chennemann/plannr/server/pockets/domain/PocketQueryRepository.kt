package de.chennemann.plannr.server.pockets.domain

interface PocketQueryRepository {
    suspend fun saveOrUpdate(pocketQuery: PocketQuery): PocketQuery
    suspend fun findById(pocketId: String): PocketQuery?
}
