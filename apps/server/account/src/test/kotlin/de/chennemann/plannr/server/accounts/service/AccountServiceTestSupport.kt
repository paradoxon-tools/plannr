package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand
import de.chennemann.plannr.server.pockets.service.PocketService

internal fun accountService(
    accountRepository: AccountRepository = InMemoryAccountRepository(),
    pocketService: RecordingPocketService = RecordingPocketService(),
): AccountService =
    AccountServiceImpl(
        accountRepository = accountRepository,
        pocketService = pocketService,
        timeProvider = { AccountFixtures.DEFAULT_CREATED_AT },
    )

internal class RecordingPocketService : PocketService {
    val archivedAccountIds = mutableListOf<Long>()
    val unarchivedAccountIds = mutableListOf<Long>()

    override suspend fun create(command: CreatePocketCommand): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun update(command: UpdatePocketCommand): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun archive(id: String): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun unarchive(id: String): Pocket = throw UnsupportedOperationException("Not used")
    override suspend fun delete(id: String) = throw UnsupportedOperationException("Not used")
    override suspend fun list(accountId: Long?, archived: Boolean?): List<Pocket> = throw UnsupportedOperationException("Not used")
    override suspend fun getById(id: String): Pocket? = throw UnsupportedOperationException("Not used")

    override suspend fun archiveForAccount(accountId: Long) {
        archivedAccountIds += accountId
    }

    override suspend fun unarchiveForAccount(accountId: Long) {
        unarchivedAccountIds += accountId
    }
}
