package de.chennemann.plannr.server.pockets.service

fun interface PocketAccountLookup {
    suspend fun exists(accountId: Long): Boolean
}
