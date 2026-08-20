package de.chennemann.plannr.ui.screen.finances.accounts.add

import de.chennemann.plannr.database.repository.AccountRepository

interface AddAccountComponent {
    suspend fun addAccount(accountName: String)

    class Factory(
        private val accountRepository: AccountRepository
    ) {
        operator fun invoke(): AddAccountComponent =
            DefaultAddAccountComponent(accountRepository)
    }
}

private class DefaultAddAccountComponent(
    private val accountRepository: AccountRepository
): AddAccountComponent {
    override suspend fun addAccount(accountName: String) {
        accountRepository.addAccount(accountName)
    }
}
