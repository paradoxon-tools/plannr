package de.chennemann.plannr.server.transactions.domain

interface TransactionRepository {
    suspend fun save(transaction: TransactionRecord): TransactionRecord
    suspend fun update(transaction: TransactionRecord): TransactionRecord
    suspend fun findById(id: String): TransactionRecord?
    suspend fun findVisibleByAccountId(accountId: String): List<TransactionRecord>
    suspend fun findVisibleByPocketId(pocketId: String): List<TransactionRecord>
    suspend fun findByRecurringTransactionId(recurringTransactionId: String): List<TransactionRecord>
    suspend fun findVisibleByRecurringTransactionId(recurringTransactionId: String): List<TransactionRecord>
    suspend fun findVisiblePending(): List<TransactionRecord>
    suspend fun findVisibleFutureByAccountId(accountId: String, startDateInclusive: String, endDateInclusive: String): List<TransactionRecord>
    suspend fun findVisibleFutureByPocketId(pocketId: String, startDateInclusive: String, endDateInclusive: String): List<TransactionRecord>
    suspend fun findAll(accountId: String? = null, pocketId: String? = null, archived: Boolean = false): List<TransactionRecord>
}
