package de.chennemann.plannr.server.contracts.domain

import de.chennemann.plannr.server.contracts.persistence.ContractModel

interface ContractRepository {
    suspend fun save(contract: ContractModel): Contract
    suspend fun update(contract: ContractModel): Contract
    suspend fun findById(id: String): Contract?
    suspend fun findByPocketId(pocketId: String): Contract?
    suspend fun findAll(accountId: String? = null, archived: Boolean = false): List<Contract>
}
