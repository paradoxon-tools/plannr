package de.chennemann.plannr.server.pockets.service

import de.chennemann.plannr.server.pockets.api.dto.Pocket

fun interface PocketAccountLookup {
    suspend fun exists(accountId: String): Boolean
}

interface PocketArchiveCascade {
    suspend fun archiveFor(pocket: Pocket)
    suspend fun unarchiveFor(pocket: Pocket)
}
