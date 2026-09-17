package de.chennemann.plannr.database.repository

import de.chennemann.plannr.data.Contract
import de.chennemann.plannr.data.Partner
import de.chennemann.plannr.database.remote.ApiContract
import de.chennemann.plannr.database.remote.ApiContractType
import de.chennemann.plannr.database.remote.ApiCreateContractCommand
import de.chennemann.plannr.database.remote.ApiUpdateContractCommand
import de.chennemann.plannr.database.remote.PlannrApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ContractRepository {
    val contracts: StateFlow<List<Contract>>

    suspend fun addContract(accountId: Long, partnerId: Long, name: String, description: String? = null): Contract.ContractId
    suspend fun updateContractName(contractId: Long, name: String)
    suspend fun updateContractDescription(contractId: Long, description: String)
    suspend fun updateContract(contract: Contract)

    companion object {
        internal operator fun invoke(apiClient: PlannrApiClient, applicationScope: CoroutineScope): ContractRepository =
            RemoteContractRepository(apiClient, applicationScope)
    }
}

private class RemoteContractRepository(
    private val apiClient: PlannrApiClient,
    private val applicationScope: CoroutineScope
): ContractRepository {
    private val refreshLock = RefreshLock()
    private var contractSnapshots = emptyMap<Long, de.chennemann.plannr.database.remote.ApiContract>()

    override val contracts = MutableStateFlow<List<Contract>>(emptyList())

    init {
        applicationScope.launchRefresh("ContractRepository.refresh") {
            refresh()
        }
    }

    override suspend fun addContract(accountId: Long, partnerId: Long, name: String, description: String?): Contract.ContractId {
        val created = apiClient.createContract(
            ApiCreateContractCommand(
                name = name,
                description = description,
                color = 0x00A896,
                type = ApiContractType.ACCUMULATING,
                accountIds = setOf(accountId),
                financialProfileId = null,
                partnerId = partnerId,
            )
        )

        val contractPocketId = apiClient.listPockets(accountId = accountId)
            .firstOrNull { it.contractId == created.id }
            ?.id
            ?: -1L

        refresh()
        return Contract.ContractId(created.id, accountId, contractPocketId)
    }

    override suspend fun updateContractName(contractId: Long, name: String) {
        val existing = contracts.value.firstOrNull { it.contractId.contractId == contractId }
            ?: run {
                refresh()
                contracts.value.firstOrNull { it.contractId.contractId == contractId }
            }
            ?: throw NoSuchElementException("No contract found for id $contractId")

        apiClient.updateContract(
            ApiUpdateContractCommand(
                id = contractId,
                financialProfileId = contractSnapshots[contractId]?.financialProfileId ?: 0L,
                partnerId = existing.partner.partnerId,
                name = name,
                description = existing.description,
                color = contractSnapshots[contractId]?.color ?: 0x00A896,
                type = contractSnapshots[contractId]?.type ?: ApiContractType.ACCUMULATING,
                signingDate = contractSnapshots[contractId]?.signingDate,
                expirationDate = contractSnapshots[contractId]?.expirationDate,
                lastCancellationDate = contractSnapshots[contractId]?.lastCancellationDate,
            )
        )

        refresh()
    }

    override suspend fun updateContractDescription(contractId: Long, description: String) {
        val existing = contracts.value.firstOrNull { it.contractId.contractId == contractId }
            ?: run {
                refresh()
                contracts.value.firstOrNull { it.contractId.contractId == contractId }
            }
            ?: throw NoSuchElementException("No contract found for id $contractId")

        apiClient.updateContract(
            ApiUpdateContractCommand(
                id = contractId,
                financialProfileId = contractSnapshots[contractId]?.financialProfileId ?: 0L,
                partnerId = existing.partner.partnerId,
                name = existing.name,
                description = description,
                color = contractSnapshots[contractId]?.color ?: 0x00A896,
                type = contractSnapshots[contractId]?.type ?: ApiContractType.ACCUMULATING,
                signingDate = contractSnapshots[contractId]?.signingDate,
                expirationDate = contractSnapshots[contractId]?.expirationDate,
                lastCancellationDate = contractSnapshots[contractId]?.lastCancellationDate,
            )
        )

        refresh()
    }

    private suspend fun refresh() {
        refreshLock.withRefreshLock {
            contracts.value = loadContracts()
        }
    }

    override suspend fun updateContract(contract: Contract) {
        require(contract.name.isNotBlank()) { "Contract name is required" }
        val id = contract.contractId.contractId
        val existing = contractSnapshots[id] ?: error("Contract is no longer available")
        val updated = apiClient.updateContract(
            ApiUpdateContractCommand(
                id = id,
                financialProfileId = existing.financialProfileId,
                partnerId = contract.partner.partnerId.takeIf { it >= 0 },
                name = contract.name.trim(),
                description = contract.description?.trim()?.ifBlank { null },
                color = contract.color,
                type = existing.type,
                signingDate = contract.signingDate,
                expirationDate = contract.expirationDate,
                lastCancellationDate = contract.lastCancellationDate,
            )
        )
        contractSnapshots = contractSnapshots + (id to updated)
        contracts.value = contracts.value.map {
            if (it.contractId.contractId == id) contract.copy(
                name = updated.name,
                description = updated.description,
                color = updated.color,
                signingDate = updated.signingDate,
                expirationDate = updated.expirationDate,
                lastCancellationDate = updated.lastCancellationDate,
            ) else it
        }
    }

    private suspend fun loadContracts(): List<Contract> {
        val partners = runCatching {
            (apiClient.listPartners() + apiClient.listPartners(archived = true)).associateBy { it.id }
        }.getOrDefault(emptyMap())
        val remoteContracts = runCatching {
            apiClient.listContracts()
        }.getOrElse {
            contractSnapshots = emptyMap()
            return emptyList()
        }
        contractSnapshots = remoteContracts.associateBy(ApiContract::id)

        return remoteContracts.map { contract ->
            Contract(
                contractId = Contract.ContractId(
                    contractId = contract.id,
                    accountId = -1L,
                    pocketId = -1L,
                ),
                partner = Partner(
                    partnerId = contract.partnerId ?: -1L,
                    name = partners[contract.partnerId]?.name ?: "Unknown partner",
                ),
                name = contract.name,
                description = contract.description,
                color = contract.color,
                signingDate = contract.signingDate,
                expirationDate = contract.expirationDate,
                lastCancellationDate = contract.lastCancellationDate,
                balance = runCatching {
                    apiClient.getContractBalance(contract.id)
                }.getOrDefault(0L),
            )
        }
    }
}
