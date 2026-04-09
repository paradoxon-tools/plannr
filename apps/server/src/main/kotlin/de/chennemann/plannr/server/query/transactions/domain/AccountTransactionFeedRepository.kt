package de.chennemann.plannr.server.query.transactions.domain

interface AccountTransactionFeedRepository {
    suspend fun findPage(accountId: String, before: Long?, limit: Int): List<AccountTransactionFeedItem>
}
