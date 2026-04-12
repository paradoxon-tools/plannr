package de.chennemann.plannr.server.recurringtransactions.support

import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository

class InMemoryRecurringTransactionRepository : RecurringTransactionRepository {
    private val values = linkedMapOf<String, RecurringTransaction>()
    override suspend fun save(recurringTransaction: RecurringTransaction): RecurringTransaction {
        values[recurringTransaction.id] = recurringTransaction
        return recurringTransaction
    }
    override suspend fun update(recurringTransaction: RecurringTransaction): RecurringTransaction {
        values[recurringTransaction.id] = recurringTransaction
        return recurringTransaction
    }
    override suspend fun findById(id: String): RecurringTransaction? = values[id]
    override suspend fun findAll(accountId: String?, contractId: String?, archived: Boolean): List<RecurringTransaction> =
        values.values.filter { it.isArchived == archived && (accountId == null || it.accountId == accountId) && (contractId == null || it.contractId == contractId) }
    override suspend fun findByContractId(contractId: String): List<RecurringTransaction> = values.values.filter { it.contractId == contractId }
    override suspend fun findByPreviousVersionId(previousVersionId: String): List<RecurringTransaction> = values.values.filter { it.previousVersionId == previousVersionId }
}
