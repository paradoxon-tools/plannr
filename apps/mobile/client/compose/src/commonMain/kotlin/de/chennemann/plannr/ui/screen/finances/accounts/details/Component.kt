package de.chennemann.plannr.ui.screen.finances.accounts.details

import de.chennemann.plannr.data.Account
import de.chennemann.plannr.database.repository.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface AccountDetailsComponent {
    val account: StateFlow<Account?>

    class Factory(
        private val accountRepository: AccountRepository
    ) {
        operator fun invoke(accountId: Long): AccountDetailsComponent =
            DefaultAccountDetailsComponent(accountRepository, accountId)
    }
}

private class DefaultAccountDetailsComponent(
    accountRepository: AccountRepository,
    accountId: Long,
): AccountDetailsComponent {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val account: StateFlow<Account?> =
        accountRepository.accounts
            .map { accounts -> accounts.firstOrNull { it.accountId == accountId } }
            .stateIn(scope, SharingStarted.Eagerly, null)
}
