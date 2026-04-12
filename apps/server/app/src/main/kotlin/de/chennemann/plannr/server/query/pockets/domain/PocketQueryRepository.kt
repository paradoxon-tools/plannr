package de.chennemann.plannr.server.query.pockets.domain

interface PocketQueryRepository {
    suspend fun saveOrUpdate(pocketQuery: PocketQuery): PocketQuery
    suspend fun findById(pocketId: String): PocketQuery?
}
