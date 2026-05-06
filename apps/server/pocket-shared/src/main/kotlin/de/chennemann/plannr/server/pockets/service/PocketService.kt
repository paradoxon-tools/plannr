package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.contracts.api.dto.Contract
import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand

interface PocketService {
    suspend fun create(command: CreatePocketCommand): Pocket
    suspend fun update(command: UpdatePocketCommand): Pocket
    suspend fun createContract(pocketId: String, command: CreateContractCommand): Contract
    suspend fun updateContract(pocketId: String, command: UpdateContractCommand): Contract
    suspend fun archive(id: String): Pocket
    suspend fun unarchive(id: String): Pocket
    suspend fun archiveForAccount(accountId: Long)
    suspend fun unarchiveForAccount(accountId: Long)
    suspend fun delete(id: String)
    suspend fun list(accountId: Long? = null, archived: Boolean? = null): List<Pocket>
    suspend fun getById(id: String): Pocket?
}
