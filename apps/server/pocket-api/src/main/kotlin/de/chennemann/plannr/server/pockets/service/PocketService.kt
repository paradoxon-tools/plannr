package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand

interface PocketService {
    suspend fun create(command: CreatePocketCommand): Pocket
    suspend fun update(command: UpdatePocketCommand): Pocket
    suspend fun archive(id: String): Pocket
    suspend fun unarchive(id: String): Pocket
    suspend fun delete(id: String)
    suspend fun list(accountId: String? = null, archived: Boolean? = null): List<Pocket>
    suspend fun getById(id: String): Pocket?
}
