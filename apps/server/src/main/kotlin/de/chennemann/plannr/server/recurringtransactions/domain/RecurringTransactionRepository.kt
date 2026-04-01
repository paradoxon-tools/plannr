package de.chennemann.plannr.server.recurringtransactions.domain

interface RecurringTransactionRepository {
    suspend fun save(recurringTransaction: RecurringTransaction): RecurringTransaction
    suspend fun update(recurringTransaction: RecurringTransaction): RecurringTransaction
    suspend fun findById(id: String): RecurringTransaction?
    suspend fun findAll(accountId: String? = null, contractId: String? = null, archived: Boolean = false): List<RecurringTransaction>
    suspend fun findByContractId(contractId: String): List<RecurringTransaction>
}
