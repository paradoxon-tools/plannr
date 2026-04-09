package de.chennemann.plannr.server.transactions.domain

interface TransactionRepository {
    suspend fun save(transaction: TransactionRecord): TransactionRecord
    suspend fun update(transaction: TransactionRecord): TransactionRecord
    suspend fun findById(id: String): TransactionRecord?
    suspend fun findVisibleByAccountId(accountId: String): List<TransactionRecord>
    suspend fun findVisibleByPocketId(pocketId: String): List<TransactionRecord>
}
