package de.chennemann.plannr.database.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import de.chennemann.plannr.data.Contract
import de.chennemann.plannr.data.Partner
import de.chennemann.plannr.database.PlannrDB
import de.chennemann.plannr.database.runGettingLastId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

interface ContractRepository {
    val contracts: StateFlow<List<Contract>>

    suspend fun addContract(accountId: Long, partnerId: Long, name: String, description: String? = null): Contract.ContractId
    suspend fun updateContractName(contractId: Long, name: String)
    suspend fun updateContractDescription(contractId: Long, description: String)

    companion object {
        operator fun invoke(plannrDB: PlannrDB, applicationScope: CoroutineScope): ContractRepository =
            CachingContractRepository(plannrDB, applicationScope)
    }
}

private class CachingContractRepository(
    private val plannrDB: PlannrDB,
    private val applicationScope: CoroutineScope
): ContractRepository {

    override val contracts: StateFlow<List<Contract>> =
        plannrDB.contractsQueries.loadContracts()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { loadContracts ->
                loadContracts.map { contract ->
                    Contract(
                        contractId = Contract.ContractId(
                            contractId = contract.contractId,
                            accountId = contract.accountId,
                            pocketId = contract.pocketId
                        ),
                        partner = Partner(
                            partnerId = contract.partnerId,
                            name = contract.partnerName
                        ),
                        name = contract.contractName,
                        description = contract.description,
                        balance = contract.balance
                    )
                }
            }
            .stateIn(
                scope = applicationScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    override suspend fun addContract(accountId: Long, partnerId: Long, name: String, description: String?): Contract.ContractId {
        return applicationScope.async(Dispatchers.IO) {
            plannrDB.transactionWithResult {

                val contractPocketId = plannrDB.runGettingLastId {
                    plannrDB.contractsQueries.addContractPocket(accountId, name)
                }

                val contractId = plannrDB.runGettingLastId {
                    plannrDB.contractsQueries.addContract(name, description, contractPocketId, partnerId)
                }

                Contract.ContractId(contractId = contractId, accountId = accountId, pocketId = contractPocketId)
            }
        }.await()
    }

    override suspend fun updateContractName(contractId: Long, name: String) {
        withContext(Dispatchers.IO) {
            plannrDB.contractsQueries.updateContractName(name, contractId)
        }
    }

    override suspend fun updateContractDescription(contractId: Long, description: String) {
        withContext(Dispatchers.IO) {
            plannrDB.contractsQueries.updateContractDescription(description, contractId)
        }
    }
}