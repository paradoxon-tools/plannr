package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand

interface PocketService {
    suspend fun create(command: CreatePocketCommand): Pocket
    suspend fun createForContract(command: CreatePocketForContractCommand): Pocket
    suspend fun update(command: UpdatePocketCommand): Pocket
    suspend fun archive(id: Long): Pocket
    suspend fun unarchive(id: Long): Pocket
    suspend fun archiveForAccount(accountId: Long)
    suspend fun unarchiveForAccount(accountId: Long)
    suspend fun delete(id: Long)
    suspend fun list(accountId: Long? = null, archived: Boolean? = null): List<Pocket>
    suspend fun getById(id: Long): Pocket?
}
