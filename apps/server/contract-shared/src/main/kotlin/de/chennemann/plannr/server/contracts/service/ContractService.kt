package de.chennemann.plannr.server.contracts.service

import de.chennemann.plannr.server.contracts.api.dto.Contract
import de.chennemann.plannr.server.contracts.api.dto.CreateContractCommand
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractCommand

interface ContractService {
    suspend fun create(command: CreateContractCommand): Contract
    suspend fun update(id: Long, command: UpdateContractCommand): Contract
    suspend fun list(accountId: Long? = null, archived: Boolean = false): List<Contract>
    suspend fun getById(id: Long): Contract?
}
