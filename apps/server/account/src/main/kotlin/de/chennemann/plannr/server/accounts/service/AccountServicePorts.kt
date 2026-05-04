package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.api.dto.Account

interface AccountArchiveCascade {
    suspend fun archiveFor(account: Account)
    suspend fun unarchiveFor(account: Account)
}
