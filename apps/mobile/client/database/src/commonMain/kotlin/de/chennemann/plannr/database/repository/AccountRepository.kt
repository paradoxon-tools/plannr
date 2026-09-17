package de.chennemann.plannr.database.repository

import de.chennemann.plannr.data.Account
import de.chennemann.plannr.data.Pocket
import de.chennemann.plannr.database.remote.ApiCreateAccountCommand
import de.chennemann.plannr.database.remote.ApiUpdateAccountCommand
import de.chennemann.plannr.database.remote.PlannrApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.supervisorScope

interface AccountRepository {
    val accounts: StateFlow<List<de.chennemann.plannr.data.Account>>

    suspend fun addAccount(accountName: String): Pocket.PocketId
    suspend fun updateAccount(accountId: Long, accountName: String)

    companion object {
        internal operator fun invoke(apiClient: PlannrApiClient, applicationScope: CoroutineScope): AccountRepository =
            RemoteAccountRepository(apiClient, applicationScope)
    }
}

private class RemoteAccountRepository(
    private val apiClient: PlannrApiClient,
    private val applicationScope: CoroutineScope
): AccountRepository {
    private val refreshLock = RefreshLock()
    private var accountSnapshots = emptyMap<Long, de.chennemann.plannr.database.remote.ApiAccount>()

    override val accounts = MutableStateFlow<List<Account>>(emptyList())

    init {
        applicationScope.launchRefresh("AccountRepository.refresh") {
            refresh()
        }
    }

    override suspend fun addAccount(accountName: String): Pocket.PocketId {
        val created = apiClient.createAccount(
            ApiCreateAccountCommand(
                name = accountName,
                institution = accountName,
                currencyCode = "EUR",
                weekendHandling = "MOVE_AFTER",
            )
        )

        val defaultPocketId = apiClient.listPockets(accountId = created.id)
            .firstOrNull { it.isDefault }
            ?.id
            ?: throw NoSuchElementException("No default pocket found for account ${created.id}")

        refresh()
        return Pocket.PocketId(created.id, defaultPocketId)
    }

    override suspend fun updateAccount(accountId: Long, accountName: String) {
        val existing = accounts.value.firstOrNull { it.accountId == accountId }
            ?: refreshAndFind(accountId)
            ?: throw NoSuchElementException("No account found for id $accountId")

        apiClient.updateAccount(
            ApiUpdateAccountCommand(
                id = accountId,
                name = accountName,
                institution = accountSnapshots[accountId]?.institution ?: existing.accountName,
                currencyCode = accountSnapshots[accountId]?.currencyCode ?: "EUR",
                weekendHandling = accountSnapshots[accountId]?.weekendHandling ?: "MOVE_AFTER"
            )
        )

        refresh()
    }

    private suspend fun refresh() {
        refreshLock.withRefreshLock {
            accounts.value = loadAccounts()
        }
    }

    private suspend fun refreshAndFind(accountId: Long): Account? {
        refresh()
        return accounts.value.firstOrNull { it.accountId == accountId }
    }

    private suspend fun loadAccounts(): List<Account> = supervisorScope {
        val apiAccounts = apiClient.listAccounts()
        val apiPockets = runCatching {
            apiClient.listPockets()
        }.getOrDefault(emptyList())
        accountSnapshots = apiAccounts.associateBy { it.id }

        apiAccounts.map { apiAccount ->
            async {
                val accountPockets = apiPockets.filter { it.accountId == apiAccount.id }
                val pocketBalances = accountPockets.associate { pocket ->
                    pocket.id to runCatching {
                        apiClient.getPocketBalance(pocket.id)
                    }.getOrDefault(0L)
                }
                val totalBalance = pocketBalances.values.sum()
                val defaultPocket = accountPockets.firstOrNull { it.isDefault }
                val freeBalance = defaultPocket?.let { pocketBalances[it.id] } ?: 0L

                Account(
                    accountId = apiAccount.id,
                    accountName = apiAccount.name,
                    pockets = accountPockets.map { pocket ->
                        Pocket(
                            id = Pocket.PocketId(apiAccount.id, pocket.id),
                            pocketName = pocket.name,
                            balance = pocketBalances[pocket.id] ?: 0L,
                            color = pocket.color,
                            contractId = pocket.contractId,
                            isDefault = pocket.isDefault,
                        )
                    },
                    totalBalance = totalBalance,
                    freeBalance = freeBalance,
                )
            }
        }.awaitAll()
    }
}
