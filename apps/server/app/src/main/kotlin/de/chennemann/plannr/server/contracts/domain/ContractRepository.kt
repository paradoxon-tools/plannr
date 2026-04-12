package de.chennemann.plannr.server.contracts.domain

interface ContractRepository {
    suspend fun save(contract: Contract): Contract
    suspend fun update(contract: Contract): Contract
    suspend fun findById(id: String): Contract?
    suspend fun findByPocketId(pocketId: String): Contract?
    suspend fun findAll(accountId: String? = null, archived: Boolean = false): List<Contract>
}
