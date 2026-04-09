package de.chennemann.plannr.server.query.transactions.domain

interface PocketTransactionFeedRepository {
    suspend fun findPage(pocketId: String, before: Long?, limit: Int): List<PocketTransactionFeedItem>
}
