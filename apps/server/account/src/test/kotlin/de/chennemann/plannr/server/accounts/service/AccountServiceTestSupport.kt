package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository

internal fun accountService(
    accountRepository: AccountRepository = InMemoryAccountRepository(),
    archiveCascade: AccountArchiveCascade = NoOpAccountArchiveCascade,
): AccountService =
    AccountServiceImpl(
        accountRepository = accountRepository,
        archiveCascade = archiveCascade,
        timeProvider = { AccountFixtures.DEFAULT_CREATED_AT },
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
