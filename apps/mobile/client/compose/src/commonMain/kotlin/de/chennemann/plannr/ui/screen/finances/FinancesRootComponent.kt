package de.chennemann.plannr.ui.screen.finances

import de.chennemann.plannr.data.Account
import de.chennemann.plannr.data.Contract
import de.chennemann.plannr.ui.screen.finances.accounts.overview.AccountOverviewComponent
import de.chennemann.plannr.ui.screen.finances.contracts.ContractOverviewComponent
import de.chennemann.plannr.ui.screen.finances.transactions.TransactionOverviewComponent

interface FinancesRootComponent {
    val accountOverview: AccountOverviewComponent
    val contractOverview: ContractOverviewComponent
    val transactionOverview: TransactionOverviewComponent

    class Factory(
        private val accountOverviewComponentFactory: AccountOverviewComponent.Factory,
        private val contractOverviewComponentFactory: ContractOverviewComponent.Factory,
        private val transactionOverviewComponentFactory: TransactionOverviewComponent.Factory,
    ) {
        operator fun invoke(
            onAccountClicked: (Account) -> Unit,
            onAddAccountRequested: () -> Unit,
            onContractClicked: (Contract) -> Unit,
            onAddContractRequested: () -> Unit,
        ): FinancesRootComponent =
            DefaultFinancesRootComponent(
                accountOverviewComponentFactory = accountOverviewComponentFactory,
                contractOverviewComponentFactory = contractOverviewComponentFactory,
                transactionOverviewComponentFactory = transactionOverviewComponentFactory,
                onAccountClicked = onAccountClicked,
                onAddAccountRequested = onAddAccountRequested,
                onContractClicked = onContractClicked,
                onAddContractRequested = onAddContractRequested,
            )
    }
}

private class DefaultFinancesRootComponent(
    accountOverviewComponentFactory: AccountOverviewComponent.Factory,
    contractOverviewComponentFactory: ContractOverviewComponent.Factory,
    transactionOverviewComponentFactory: TransactionOverviewComponent.Factory,
    onAccountClicked: (Account) -> Unit,
    onAddAccountRequested: () -> Unit,
    onContractClicked: (Contract) -> Unit,
    onAddContractRequested: () -> Unit,
) : FinancesRootComponent {
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
