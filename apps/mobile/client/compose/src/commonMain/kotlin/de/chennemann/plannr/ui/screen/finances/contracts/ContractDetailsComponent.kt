package de.chennemann.plannr.ui.screen.finances.contracts

import de.chennemann.plannr.database.repository.ContractRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface ContractDetailsComponent {
    val contract: StateFlow<de.chennemann.plannr.data.Contract?>

    class Factory(
        private val contractRepository: ContractRepository,
    ) {
        operator fun invoke(contractId: Long): ContractDetailsComponent =
            DefaultContractDetailsComponent(contractRepository, contractId)
    }
}

private class DefaultContractDetailsComponent(
    contractRepository: ContractRepository,
    contractId: Long,
): ContractDetailsComponent {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val contract: StateFlow<de.chennemann.plannr.data.Contract?> =
        contractRepository.contracts
            .map { contracts -> contracts.firstOrNull { it.contractId.contractId == contractId } }
            .stateIn(scope, SharingStarted.Eagerly, null)
}
