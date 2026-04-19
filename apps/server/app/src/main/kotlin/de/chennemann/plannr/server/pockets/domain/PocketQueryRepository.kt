package de.chennemann.plannr.server.pockets.domain

interface PocketQueryRepository {
    suspend fun saveOrUpdate(pocketQuery: PocketQuery): PocketQuery
    suspend fun findById(pocketId: String): PocketQuery?
    suspend fun findAll(accountId: String? = null, archived: Boolean = false): List<PocketQuery>
}
