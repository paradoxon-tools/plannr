package de.chennemann.plannr.server.transactions.projection.service

import de.chennemann.plannr.server.transactions.projection.api.dto.TransactionFeedResponse

interface TransactionFeedService {
    suspend fun getForAccount(id: Long, cursor: String?, limit: Int): TransactionFeedResponse
    suspend fun getForPocket(id: Long, cursor: String?, limit: Int): TransactionFeedResponse
    suspend fun getForContract(id: Long, cursor: String?, limit: Int): TransactionFeedResponse
}
