package de.chennemann.plannr.ui.screen.finances.contracts

import de.chennemann.plannr.database.repository.ContractRepository
import de.chennemann.plannr.database.repository.PartnerRepository
import de.chennemann.plannr.data.Contract
import de.chennemann.plannr.data.Partner
import de.chennemann.plannr.database.repository.TransactionRepository
import de.chennemann.plannr.ui.screen.finances.transactions.TransactionOverviewComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface ContractDetailsComponent {
    val contract: StateFlow<de.chennemann.plannr.data.Contract?>
    val partners: StateFlow<List<Partner>>
    val transactionOverview: TransactionOverviewComponent
    suspend fun save(contract: Contract)

    class Factory(
        private val contractRepository: ContractRepository,
        private val partnerRepository: PartnerRepository,
        private val transactionRepository: TransactionRepository,
    ) {
        operator fun invoke(contractId: Long): ContractDetailsComponent =
            DefaultContractDetailsComponent(contractRepository, partnerRepository, transactionRepository, contractId)
    }
}

private class DefaultContractDetailsComponent(
    private val contractRepository: ContractRepository,
    private val partnerRepository: PartnerRepository,
    transactionRepository: TransactionRepository,
    contractId: Long,
): ContractDetailsComponent {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override val partners = partnerRepository.partners
    override suspend fun save(contract: Contract) = contractRepository.updateContract(contract)
    override val transactionOverview = object : TransactionOverviewComponent {
        override val history = kotlinx.coroutines.flow.MutableStateFlow(emptyList<de.chennemann.plannr.data.Transaction>())
        override val loading = transactionRepository.loading
        override val error = transactionRepository.error
        override suspend fun refresh() = transactionRepository.refresh()
        override val transactions = transactionRepository.transactions
            .map { transactions -> transactions.filter { it.contractId == contractId }.distinctBy { it.id } }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())
    }

    override val contract: StateFlow<de.chennemann.plannr.data.Contract?> =
        combine(contractRepository.contracts, partnerRepository.allPartners) { contracts, partners ->
            contracts.firstOrNull { it.contractId.contractId == contractId }?.let { contract ->
                contract.copy(partner = partners.firstOrNull { it.partnerId == contract.partner.partnerId } ?: contract.partner)
            }
        }
            .stateIn(scope, SharingStarted.Eagerly, null)
}
