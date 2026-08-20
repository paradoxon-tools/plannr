package de.chennemann.plannr.database.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import de.chennemann.plannr.data.Account
import de.chennemann.plannr.data.Pocket
import de.chennemann.plannr.database.PlannrDB
import de.chennemann.plannr.database.runGettingLastId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

interface AccountRepository {
    val accounts: StateFlow<List<de.chennemann.plannr.data.Account>>

    suspend fun addAccount(accountName: String): Pocket.PocketId
    suspend fun updateAccount(accountId: Long, accountName: String)

    companion object {
        operator fun invoke(plannrDB: PlannrDB, applicationScope: CoroutineScope): AccountRepository =
            CachingAccountRepository(plannrDB, applicationScope)
    }
}

private class CachingAccountRepository(
    private val plannrDB: PlannrDB,
    private val applicationScope: CoroutineScope
): AccountRepository {

    override val accounts: StateFlow<List<de.chennemann.plannr.data.Account>> =
        plannrDB.accountsQueries.loadAccounts()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { loadAccounts ->
                loadAccounts.groupBy { it.accountId to it.accountName }.map { (account, accountPockets) ->
                    val (accountId, accountName) = account
                    Account(
                        accountId = accountId,
                        accountName = accountName,
                        totalBalance = accountPockets.sumOf { it.balance },
                        freeBalance = accountPockets.single { it.isDefault }.balance,
                        pockets = accountPockets.map { pocket ->
                            Pocket(
                                id = Pocket.PocketId(
                                    accountId = accountId,
                                    pocketId = pocket.pocketId
                                ),
                                pocketName = pocket.pocketName,
                                balance = pocket.balance
                            )
                        }
                    )
                }
            }
            .stateIn(
                scope = applicationScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    override suspend fun addAccount(accountName: String): Pocket.PocketId {
        return applicationScope.async(Dispatchers.IO) {
            plannrDB.transactionWithResult {
                val accountId = plannrDB.runGettingLastId {
                    plannrDB.accountsQueries.addAccount(accountName)
                }

                val defaultAccountPocketId = plannrDB.runGettingLastId {
                    plannrDB.accountsQueries.addDefaultAccountPocket(accountId, accountName)
                }

                Pocket.PocketId(accountId, defaultAccountPocketId)
            }
        }.await()
    }

    override suspend fun updateAccount(accountId: Long, accountName: String) {
        withContext(Dispatchers.IO) {
            plannrDB.accountsQueries.updateAccount(accountName, accountId)
        }
    }
}