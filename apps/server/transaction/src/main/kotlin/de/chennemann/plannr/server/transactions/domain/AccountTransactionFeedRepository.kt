package de.chennemann.plannr.server.transactions.domain

interface AccountTransactionFeedRepository {
    suspend fun findPage(accountId: String, before: Long?, limit: Int): List<AccountTransactionFeedItem>
}
