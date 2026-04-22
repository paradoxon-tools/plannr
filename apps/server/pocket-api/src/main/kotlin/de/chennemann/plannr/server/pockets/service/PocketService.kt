package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.domain.PocketQuery

interface PocketService {
    suspend fun create(command: CreatePocketCommand): Pocket
    suspend fun update(command: UpdatePocketCommand): Pocket
    suspend fun archive(id: String): Pocket
    suspend fun unarchive(id: String): Pocket
    suspend fun list(accountId: String? = null, archived: Boolean? = null): List<Pocket>
    suspend fun getById(id: String): Pocket?
    suspend fun listQueries(accountId: String? = null, archived: Boolean = false): List<PocketQuery>
    suspend fun getQuery(id: String): PocketQuery
}

data class CreatePocketCommand(
    val accountId: String,
    val name: String,
    val description: String?,
    val color: Int,
    val isDefault: Boolean,
)

data class UpdatePocketCommand(
    val id: String,
    val accountId: String,
    val name: String,
    val description: String?,
    val color: Int,
    val isDefault: Boolean,
)
