package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus

internal fun accountService(
    accountRepository: AccountRepository = InMemoryAccountRepository(),
    archiveCascade: AccountArchiveCascade = NoOpAccountArchiveCascade,
    balanceProvider: AccountBalanceProvider = AccountBalanceProvider { 0 },
): AccountService =
    AccountServiceImpl(
        accountRepository = accountRepository,
        archiveCascade = archiveCascade,
        balanceProvider = balanceProvider,
        timeProvider = { AccountFixtures.DEFAULT_CREATED_AT },
        applicationEventBus = NoOpApplicationEventBus,
    )

private object NoOpAccountArchiveCascade : AccountArchiveCascade {
    override suspend fun archiveFor(account: Account) = Unit
    override suspend fun unarchiveFor(account: Account) = Unit
}

internal class RecordingAccountArchiveCascade : AccountArchiveCascade {
    val archivedAccountIds = mutableListOf<String>()
    val unarchivedAccountIds = mutableListOf<String>()

    override suspend fun archiveFor(account: Account) {
        archivedAccountIds += account.id
    }

    override suspend fun unarchiveFor(account: Account) {
        unarchivedAccountIds += account.id
    }
}
