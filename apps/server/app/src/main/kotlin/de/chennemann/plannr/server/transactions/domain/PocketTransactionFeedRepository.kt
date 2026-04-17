package de.chennemann.plannr.server.transactions.domain

interface PocketTransactionFeedRepository {
    suspend fun findPage(pocketId: String, before: Long?, limit: Int): List<PocketTransactionFeedItem>
}
