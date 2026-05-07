package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.PocketWithContract
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand

interface ContractService {
    suspend fun create(pocket: Pocket, command: CreateContractCommand): PocketWithContract
    suspend fun update(pocket: Pocket, command: UpdateContractCommand): PocketWithContract
    suspend fun archiveForPocket(pocketId: Long)
    suspend fun unarchiveForPocket(pocketId: Long)
    suspend fun delete(pocketId: Long)
    suspend fun list(accountId: Long? = null, archived: Boolean = false): List<PocketWithContract>
}
