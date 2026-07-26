package de.chennemann.plannr.server.savinggoals.service

import de.chennemann.plannr.server.accounts.api.dto.Account
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountCommand
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountCommand
import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.financialprofiles.api.dto.CreateFinancialProfileCommand
import de.chennemann.plannr.server.financialprofiles.api.dto.FinancialProfile
import de.chennemann.plannr.server.financialprofiles.api.dto.UpdateFinancialProfileCommand
import de.chennemann.plannr.server.financialprofiles.service.FinancialProfileService
import de.chennemann.plannr.server.pockets.api.dto.CreatePocketCommand
import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketCommand
import de.chennemann.plannr.server.pockets.service.CreatePocketForContractCommand
import de.chennemann.plannr.server.pockets.service.CreatePocketForSavingGoalCommand
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.pockets.service.UpdatePocketsForSavingGoalCommand
import de.chennemann.plannr.server.savinggoals.support.InMemorySavingGoalRepository
import de.chennemann.plannr.server.savinggoals.support.SavingGoalFixtures
import de.chennemann.plannr.server.transactions.projection.api.dto.TransactionFeedResponse
import de.chennemann.plannr.server.transactions.projection.service.TransactionFeedService

internal class FakeFinancialProfileService : FinancialProfileService {
    private val profile = FinancialProfile(
        id = SavingGoalFixtures.DEFAULT_FINANCIAL_PROFILE_ID,
        name = "Household",
        description = null,
        isDefault = true,
        isFallback = true,
        isArchived = false,
        createdAt = SavingGoalFixtures.DEFAULT_CREATED_AT,
    )

    override suspend fun resolveForAssignment(id: Long?): FinancialProfile = profile.copy(id = id ?: profile.id)
    override suspend fun getById(id: Long): FinancialProfile? = profile.takeIf { it.id == id }
    override suspend fun create(command: CreateFinancialProfileCommand): FinancialProfile = unsupported()
    override suspend fun update(command: UpdateFinancialProfileCommand): FinancialProfile = unsupported()
    override suspend fun makeDefault(id: Long): FinancialProfile = unsupported()
    override suspend fun archive(id: Long): FinancialProfile = unsupported()
    override suspend fun unarchive(id: Long): FinancialProfile = unsupported()
    override suspend fun delete(id: Long) = unsupported<Unit>()
    override suspend fun list(query: String?, archived: Boolean): List<FinancialProfile> = listOf(profile)

    private fun <T> unsupported(): T = throw UnsupportedOperationException("Not used")
}

internal class FakeAccountService(
    accounts: Iterable<Account> = listOf(account()),
) : AccountService {
    private val accounts = accounts.associateBy { it.id }

    override suspend fun getById(id: Long): Account? = accounts[id]
    override suspend fun create(command: CreateAccountCommand): Account = unsupported()
    override suspend fun update(command: UpdateAccountCommand): Account = unsupported()
    override suspend fun archive(id: Long): Account = unsupported()
    override suspend fun unarchive(id: Long): Account = unsupported()
    override suspend fun delete(id: Long) = unsupported<Unit>()
    override suspend fun list(archived: Boolean?): List<Account> = accounts.values.toList()

    private fun <T> unsupported(): T = throw UnsupportedOperationException("Not used")

    companion object {
        fun account(
            id: Long = SavingGoalFixtures.DEFAULT_ACCOUNT_ID,
            currencyCode: String = "EUR",
        ) = Account(
            id = id,
            name = "Account $id",
            institution = "Test",
            currencyCode = currencyCode,
            weekendHandling = "NO_SHIFT",
            isArchived = false,
            createdAt = SavingGoalFixtures.DEFAULT_CREATED_AT,
        )
    }
}

internal class FakePocketService : PocketService {
    private val pockets = linkedMapOf<Long, Pocket>()
    val createCommands = mutableListOf<CreatePocketForSavingGoalCommand>()
    val updateCommands = mutableListOf<UpdatePocketsForSavingGoalCommand>()

    override suspend fun createForSavingGoal(command: CreatePocketForSavingGoalCommand): Pocket {
        createCommands += command
        val id = (pockets.keys.maxOrNull() ?: 0L) + 1L
        return Pocket(
            id = id,
            accountId = command.accountId,
            savingGoalId = command.savingGoalId,
            name = command.name,
            description = command.description,
            color = command.color,
            isDefault = false,
            isArchived = false,
            createdAt = SavingGoalFixtures.DEFAULT_CREATED_AT,
        ).also { pockets[id] = it }
    }

    override suspend fun updateForSavingGoal(command: UpdatePocketsForSavingGoalCommand) {
        updateCommands += command
        pockets.replaceAll { _, pocket ->
            if (pocket.savingGoalId != command.savingGoalId) pocket else pocket.copy(
                name = command.name,
                description = command.description,
                color = command.color,
            )
        }
    }

    override suspend fun archiveForSavingGoal(savingGoalId: Long) {
        setArchived(savingGoalId, true)
    }

    override suspend fun unarchiveForSavingGoal(savingGoalId: Long) {
        setArchived(savingGoalId, false)
    }

    override suspend fun listForSavingGoal(savingGoalId: Long): List<Pocket> =
        pockets.values.filter { it.savingGoalId == savingGoalId }

    override suspend fun create(command: CreatePocketCommand): Pocket = unsupported()
    override suspend fun createForContract(command: CreatePocketForContractCommand): Pocket = unsupported()
    override suspend fun update(command: UpdatePocketCommand): Pocket = unsupported()
    override suspend fun archive(id: Long): Pocket = unsupported()
    override suspend fun unarchive(id: Long): Pocket = unsupported()
    override suspend fun archiveForAccount(accountId: Long) = unsupported<Unit>()
    override suspend fun unarchiveForAccount(accountId: Long) = unsupported<Unit>()
    override suspend fun delete(id: Long) = unsupported<Unit>()
    override suspend fun list(accountId: Long?, archived: Boolean?): List<Pocket> = pockets.values.toList()
    override suspend fun getById(id: Long): Pocket? = pockets[id]

    private fun setArchived(savingGoalId: Long, archived: Boolean) {
        pockets.replaceAll { _, pocket ->
            if (pocket.savingGoalId == savingGoalId) pocket.copy(isArchived = archived) else pocket
        }
    }

    private fun <T> unsupported(): T = throw UnsupportedOperationException("Not used")
}

internal class FakeTransactionFeedService(
    val balances: MutableMap<Long, Long> = mutableMapOf(),
) : TransactionFeedService {
    override suspend fun getForPocket(id: Long, cursor: String?, limit: Int): TransactionFeedResponse =
        response(balances[id] ?: 0L)

    override suspend fun getForAccount(id: Long, cursor: String?, limit: Int): TransactionFeedResponse = response()
    override suspend fun getForContract(id: Long, cursor: String?, limit: Int): TransactionFeedResponse = response()

    private fun response(balance: Long = 0L) =
        TransactionFeedResponse(balance, emptyList(), nextCursor = null, hasMore = false)
}

internal fun savingGoalService(
    repository: InMemorySavingGoalRepository = InMemorySavingGoalRepository(),
    accounts: AccountService = FakeAccountService(),
    pockets: FakePocketService = FakePocketService(),
    feeds: FakeTransactionFeedService = FakeTransactionFeedService(),
) = SavingGoalServiceImpl(
    savingGoalRepository = repository,
    financialProfileService = FakeFinancialProfileService(),
    accountService = accounts,
    pocketService = pockets,
    transactionFeedService = feeds,
    timeProvider = { SavingGoalFixtures.DEFAULT_CREATED_AT },
)
