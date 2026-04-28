package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.contracts.persistence.ContractModel
import de.chennemann.plannr.server.contracts.persistence.toModel

class InMemoryContractRepository(
    private val accountIdResolver: (String) -> String = { pocketId -> pocketId.replaceFirst("poc_", "acc_") },
) : ContractRepository {
    private val contracts = linkedMapOf<String, Contract>()

    override suspend fun save(contract: ContractModel): Contract {
        val persisted = contract.toDomain("con_${contracts.size + 1}")
        contracts[persisted.id] = persisted
        return persisted
    }

    override suspend fun update(contract: ContractModel): Contract {
        val persisted = contract.toDomain("con_${contracts.size + 1}")
        contracts[persisted.id] = persisted
        return persisted
    }

    override suspend fun findById(id: String): Contract? = contracts[id]

    override suspend fun findByPocketId(pocketId: String): Contract? =
        contracts.values.firstOrNull { it.pocketId == pocketId }

    override suspend fun findAll(accountId: String?, archived: Boolean): List<Contract> =
        contracts.values.filter { contract ->
            contract.isArchived == archived &&
                (accountId == null || contract.accountId == accountId)
        }

    suspend fun save(contract: Contract): Contract {
        contracts[contract.id] = contract
        return contract
    }

    suspend fun update(contract: Contract): Contract {
        contracts[contract.id] = contract
        return contract
    }

    fun peekByPocketId(pocketId: String): Contract? =
        contracts.values.firstOrNull { it.pocketId == pocketId }

    private fun ContractModel.toDomain(fallbackId: String): Contract =
        Contract(
            id = id ?: fallbackId,
            accountId = accountIdResolver(pocketId),
            pocketId = pocketId,
            partnerId = partnerId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
            isArchived = isArchived,
            createdAt = createdAt,
        )
}
