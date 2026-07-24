package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountCommand
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountCommand
import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.UpdateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService

internal object NoOpTransactionTemplateService : TransactionTemplateService {
    override suspend fun create(command: CreateTransactionTemplateCommand) = throw UnsupportedOperationException("Not used")
    override suspend fun createBatch(commands: List<CreateTransactionTemplateCommand>) = throw UnsupportedOperationException("Not used")
    override suspend fun update(command: UpdateTransactionTemplateCommand) = throw UnsupportedOperationException("Not used")
    override suspend fun archive(id: Long) = throw UnsupportedOperationException("Not used")
    override suspend fun unarchive(id: Long) = throw UnsupportedOperationException("Not used")
    override suspend fun archiveForPocket(pocketId: Long) = Unit
    override suspend fun unarchiveForPocket(pocketId: Long) = Unit
    override suspend fun refreshFinancialProfilesForPocket(pocketId: Long) = Unit
    override suspend fun delete(id: Long) = Unit
    override suspend fun list(archived: Boolean?): List<TransactionTemplate> = emptyList()
    override suspend fun getById(id: Long) = null
}

internal class StubAccountService(
    private val exists: (Long) -> Boolean = { true },
) : AccountService {
    override suspend fun create(command: CreateAccountCommand): Account = unsupported()
    override suspend fun update(command: UpdateAccountCommand): Account = unsupported()
    override suspend fun archive(id: Long): Account = unsupported()
    override suspend fun unarchive(id: Long): Account = unsupported()
    override suspend fun delete(id: Long) = unsupported<Unit>()
    override suspend fun list(archived: Boolean?): List<Account> = emptyList()
    override suspend fun getById(id: Long): Account? =
        id.takeIf(exists)?.let {
            Account(
                id = it,
                name = "Account $it",
                institution = "Test",
                currencyCode = "EUR",
                weekendHandling = "NO_SHIFT",
                isArchived = false,
                createdAt = PocketFixtures.DEFAULT_CREATED_AT,
            )
        }

    private fun <T> unsupported(): T = throw UnsupportedOperationException("Not used")
}

internal fun pocketService(
    repository: InMemoryPocketRepository = InMemoryPocketRepository(),
    accountService: AccountService = StubAccountService(),
    transactionTemplateService: TransactionTemplateService = NoOpTransactionTemplateService,
): PocketServiceImpl =
    PocketServiceImpl(
        pocketRepository = repository,
        accountService = accountService,
        transactionTemplateService = transactionTemplateService,
        timeProvider = { PocketFixtures.DEFAULT_CREATED_AT },
    )
