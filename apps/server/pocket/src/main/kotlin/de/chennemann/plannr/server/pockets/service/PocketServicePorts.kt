package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.pockets.domain.Pocket

fun interface PocketAccountLookup {
    suspend fun exists(accountId: String): Boolean
}

interface PocketArchiveCascade {
    suspend fun archiveFor(pocket: Pocket)
    suspend fun unarchiveFor(pocket: Pocket)
}

fun interface PocketBalanceProvider {
    suspend fun currentBalance(pocketId: String): Long
}
