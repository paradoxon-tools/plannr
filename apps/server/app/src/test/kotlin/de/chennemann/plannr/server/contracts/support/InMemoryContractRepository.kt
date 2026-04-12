package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository

class InMemoryContractRepository : ContractRepository {
    private val contracts = linkedMapOf<String, Contract>()

    override suspend fun save(contract: Contract): Contract {
        contracts[contract.id] = contract
        return contract
    }

    override suspend fun update(contract: Contract): Contract {
        contracts[contract.id] = contract
        return contract
    }

    override suspend fun findById(id: String): Contract? = contracts[id]

    override suspend fun findByPocketId(pocketId: String): Contract? =
        contracts.values.firstOrNull { it.pocketId == pocketId }

    override suspend fun findAll(accountId: String?, archived: Boolean): List<Contract> =
        contracts.values.filter { contract ->
            contract.isArchived == archived &&
                (accountId == null || contract.accountId == accountId)
        }
}
