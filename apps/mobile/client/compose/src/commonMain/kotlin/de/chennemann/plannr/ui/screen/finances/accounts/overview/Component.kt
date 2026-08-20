package de.chennemann.plannr.ui.screen.finances.accounts.overview

import de.chennemann.plannr.data.Account
import de.chennemann.plannr.database.repository.AccountRepository
import kotlinx.coroutines.flow.StateFlow

interface AccountOverviewComponent {
    val accounts: StateFlow<List<Account>>

    suspend fun addAccount()
    fun onAccountClicked(account: Account)

    class Factory(
        private val accountRepository: AccountRepository
    ) {
        operator fun invoke(
            onAccountClicked: (Account) -> Unit,
            onAddAccountRequested: () -> Unit,
        ): AccountOverviewComponent =
            DefaultAccountOverviewComponent(
                accountRepository = accountRepository,
                onAccountClicked = onAccountClicked,
                onAddAccountRequested = onAddAccountRequested,
            )
    }
}

private class DefaultAccountOverviewComponent(
    private val accountRepository: AccountRepository,
    private val onAccountClicked: (Account) -> Unit,
    private val onAddAccountRequested: () -> Unit,
): AccountOverviewComponent {
    override val accounts = accountRepository.accounts

    override suspend fun addAccount() {
        onAddAccountRequested()
    }

    override fun onAccountClicked(account: Account) {
        onAccountClicked.invoke(account)
    }
}
