package de.chennemann.plannr.ui.screen.finances

import de.chennemann.plannr.data.Account
import de.chennemann.plannr.data.Contract
import de.chennemann.plannr.ui.screen.finances.accounts.overview.AccountOverviewComponent
import de.chennemann.plannr.ui.screen.finances.contracts.ContractOverviewComponent
import de.chennemann.plannr.ui.screen.finances.transactions.TransactionOverviewComponent

interface FinancesRootComponent {
    fun openManagement()
    val partners: kotlinx.coroutines.flow.StateFlow<List<de.chennemann.plannr.data.Partner>>
    val accountOverview: AccountOverviewComponent
    val contractOverview: ContractOverviewComponent
    val transactionOverview: TransactionOverviewComponent

    class Factory(
        private val partnerRepository: de.chennemann.plannr.database.repository.PartnerRepository,
        private val accountOverviewComponentFactory: AccountOverviewComponent.Factory,
        private val contractOverviewComponentFactory: ContractOverviewComponent.Factory,
        private val transactionOverviewComponentFactory: TransactionOverviewComponent.Factory,
    ) {
        operator fun invoke(
            onAccountClicked: (Account) -> Unit,
            onAddAccountRequested: () -> Unit,
            onContractClicked: (Contract) -> Unit,
            onAddContractRequested: () -> Unit,
            onManagementRequested: () -> Unit,
        ): FinancesRootComponent =
            DefaultFinancesRootComponent(
                partnerRepository = partnerRepository,
                accountOverviewComponentFactory = accountOverviewComponentFactory,
                contractOverviewComponentFactory = contractOverviewComponentFactory,
                transactionOverviewComponentFactory = transactionOverviewComponentFactory,
                onAccountClicked = onAccountClicked,
                onAddAccountRequested = onAddAccountRequested,
                onContractClicked = onContractClicked,
                onAddContractRequested = onAddContractRequested,
                onManagementRequested = onManagementRequested,
            )
    }
}

private class DefaultFinancesRootComponent(
    partnerRepository: de.chennemann.plannr.database.repository.PartnerRepository,
    accountOverviewComponentFactory: AccountOverviewComponent.Factory,
    contractOverviewComponentFactory: ContractOverviewComponent.Factory,
    transactionOverviewComponentFactory: TransactionOverviewComponent.Factory,
    onAccountClicked: (Account) -> Unit,
    onAddAccountRequested: () -> Unit,
    onContractClicked: (Contract) -> Unit,
    onAddContractRequested: () -> Unit,
    private val onManagementRequested: () -> Unit,
) : FinancesRootComponent {
    override fun openManagement() = onManagementRequested()
    override val partners = partnerRepository.allPartners
    override val accountOverview = accountOverviewComponentFactory(
        onAccountClicked = onAccountClicked,
        onAddAccountRequested = onAddAccountRequested,
    )

    override val contractOverview = contractOverviewComponentFactory(
        onContractClicked = onContractClicked,
        onAddContractRequested = onAddContractRequested,
    )

    override val transactionOverview = transactionOverviewComponentFactory()
}
