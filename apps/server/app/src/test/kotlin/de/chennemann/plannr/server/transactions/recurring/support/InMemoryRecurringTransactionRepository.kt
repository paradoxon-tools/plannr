package de.chennemann.plannr.server.transactions.recurring.support

import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.persistence.RecurringTransactionModel
import de.chennemann.plannr.server.transactions.recurring.persistence.toModel

class InMemoryRecurringTransactionRepository(
    private val contractIdResolver: (RecurringTransactionModel) -> String? = { null },
    private val accountIdResolver: (RecurringTransactionModel) -> String = { model ->
        (model.sourcePocketId ?: model.destinationPocketId)?.replaceFirst("poc_", "acc_") ?: "acc_unknown"
    },
) : RecurringTransactionRepository {
    private val values = linkedMapOf<String, RecurringTransaction>()
    override suspend fun save(recurringTransaction: RecurringTransactionModel): RecurringTransaction {
        val persisted = recurringTransaction.toDomain("rtx_${values.size + 1}")
        values[persisted.id] = persisted
        return persisted
    }
    override suspend fun update(recurringTransaction: RecurringTransactionModel): RecurringTransaction {
        val persisted = recurringTransaction.toDomain("rtx_${values.size + 1}")
        values[persisted.id] = persisted
        return persisted
    }
    override suspend fun findById(id: String): RecurringTransaction? = values[id]
    override suspend fun findAll(accountId: String?, contractId: String?, archived: Boolean): List<RecurringTransaction> =
        values.values.filter { it.isArchived == archived && (accountId == null || it.accountId == accountId) && (contractId == null || it.contractId == contractId) }
    override suspend fun findByContractId(contractId: String): List<RecurringTransaction> = values.values.filter { it.contractId == contractId }
    override suspend fun findByPreviousVersionId(previousVersionId: String): List<RecurringTransaction> = values.values.filter { it.previousVersionId == previousVersionId }

    suspend fun save(recurringTransaction: RecurringTransaction): RecurringTransaction {
        values[recurringTransaction.id] = recurringTransaction
        return recurringTransaction
    }

    suspend fun update(recurringTransaction: RecurringTransaction): RecurringTransaction {
        values[recurringTransaction.id] = recurringTransaction
        return recurringTransaction
    }

    private fun RecurringTransactionModel.toDomain(fallbackId: String): RecurringTransaction {
        return RecurringTransaction(
            id = id ?: fallbackId,
            contractId = contractIdResolver(this),
            accountId = accountIdResolver(this),
            sourcePocketId = sourcePocketId,
            destinationPocketId = destinationPocketId,
            partnerId = partnerId,
            title = title,
            description = description,
            amount = amount,
            currencyCode = currencyCode,
            transactionType = transactionType,
            firstOccurrenceDate = firstOccurrenceDate,
            finalOccurrenceDate = finalOccurrenceDate,
            recurrenceType = recurrenceType,
            skipCount = skipCount,
            daysOfWeek = daysOfWeek,
            weeksOfMonth = weeksOfMonth,
            daysOfMonth = daysOfMonth,
            monthsOfYear = monthsOfYear,
            lastMaterializedDate = lastMaterializedDate,
            previousVersionId = previousVersionId,
            isArchived = isArchived,
            createdAt = createdAt,
        )
    }
}
