package de.chennemann.plannr.server.transactions.recurring.domain

import de.chennemann.plannr.server.transactions.recurring.persistence.RecurringTransactionModel

interface RecurringTransactionRepository {
    suspend fun save(recurringTransaction: RecurringTransactionModel): RecurringTransaction
    suspend fun update(recurringTransaction: RecurringTransactionModel): RecurringTransaction
    suspend fun findById(id: String): RecurringTransaction?
    suspend fun findAll(accountId: String? = null, contractId: String? = null, archived: Boolean = false): List<RecurringTransaction>
    suspend fun findByContractId(contractId: String): List<RecurringTransaction>
    suspend fun findByPreviousVersionId(previousVersionId: String): List<RecurringTransaction>
}
