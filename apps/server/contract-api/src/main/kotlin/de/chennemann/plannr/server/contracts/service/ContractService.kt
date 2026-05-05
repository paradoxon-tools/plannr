package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.contracts.api.dto.Contract
import de.chennemann.plannr.server.contracts.api.dto.CreateContractCommand
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractCommand

interface ContractService {
    suspend fun create(command: CreateContractCommand): Contract
    suspend fun update(command: UpdateContractCommand): Contract
    suspend fun archive(id: String): Contract
    suspend fun unarchive(id: String): Contract
    suspend fun delete(id: String)
    suspend fun list(accountId: String? = null, archived: Boolean = false): List<Contract>
}
