package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.domain.Account

interface AccountArchiveCascade {
    suspend fun archiveFor(account: Account)
    suspend fun unarchiveFor(account: Account)
}

fun interface AccountBalanceProvider {
    suspend fun currentBalance(accountId: String): Long
}
