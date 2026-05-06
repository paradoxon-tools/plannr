package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.contracts.api.dto.Contract
import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand

interface ContractService {
    suspend fun create(pocket: Pocket, command: CreateContractCommand): Contract
    suspend fun update(pocket: Pocket, command: UpdateContractCommand): Contract
    suspend fun archiveForPocket(pocketId: String)
    suspend fun unarchiveForPocket(pocketId: String)
    suspend fun delete(id: String)
    suspend fun list(accountId: Long? = null, archived: Boolean = false): List<Contract>
}
