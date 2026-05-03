package de.chennemann.plannr.server.transactions.support

import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import de.chennemann.plannr.server.transactions.domain.TransactionVisibility
import de.chennemann.plannr.server.transactions.persistence.TransactionModel
import de.chennemann.plannr.server.transactions.persistence.toModel

class InMemoryTransactionRepository : TransactionRepository {
    private val values = linkedMapOf<String, TransactionRecord>()

    override suspend fun save(transaction: TransactionModel): TransactionRecord {
        val persisted = transaction.toDomain("txn_${values.size + 1}")
        if (
            values.values.any {
                it.recurringTransactionId == persisted.recurringTransactionId &&
                    it.transactionDate == persisted.transactionDate &&
                    it.parentTransactionId == null &&
                    persisted.parentTransactionId == null &&
                    persisted.recurringTransactionId != null
            }
        ) {
            throw IllegalStateException("duplicate recurring root occurrence")
        }
        values[persisted.id] = persisted
        return persisted
    }

    override suspend fun update(transaction: TransactionModel): TransactionRecord {
        val persisted = transaction.toDomain("txn_${values.size + 1}")
        values[persisted.id] = persisted
        return persisted
    }

    override suspend fun findById(id: String): TransactionRecord? = values[id]

    override suspend fun findVisibleByAccountId(accountId: String): List<TransactionRecord> =
        values.values.filter { it.accountId == accountId && TransactionVisibility.includes(it) }.sortedBy { it.transactionDate }

    override suspend fun findVisibleByPocketId(pocketId: String): List<TransactionRecord> =
        values.values.filter {
            (it.pocketId == pocketId || it.sourcePocketId == pocketId || it.destinationPocketId == pocketId) && TransactionVisibility.includes(it)
        }.sortedBy { it.transactionDate }

    override suspend fun findByRecurringTransactionId(recurringTransactionId: String): List<TransactionRecord> =
        values.values.filter { it.recurringTransactionId == recurringTransactionId }.sortedBy { it.transactionDate }

    override suspend fun findVisibleByRecurringTransactionId(recurringTransactionId: String): List<TransactionRecord> =
        values.values.filter { it.recurringTransactionId == recurringTransactionId && TransactionVisibility.includes(it) }.sortedBy { it.transactionDate }

    override suspend fun findVisiblePending(): List<TransactionRecord> =
        values.values.filter { it.status == "PENDING" && TransactionVisibility.includes(it) }.sortedBy { it.transactionDate }

    override suspend fun findVisibleFutureByAccountId(accountId: String, startDateInclusive: String, endDateInclusive: String): List<TransactionRecord> =
        findVisibleByAccountId(accountId).filter { it.transactionDate in startDateInclusive..endDateInclusive }

    override suspend fun findVisibleFutureByPocketId(pocketId: String, startDateInclusive: String, endDateInclusive: String): List<TransactionRecord> =
        findVisibleByPocketId(pocketId).filter { it.transactionDate in startDateInclusive..endDateInclusive }

    override suspend fun findAll(accountId: String?, pocketId: String?, archived: Boolean): List<TransactionRecord> =
        values.values
            .filter { accountId == null || it.accountId == accountId }
            .filter { pocketId == null || it.pocketId == pocketId || it.sourcePocketId == pocketId || it.destinationPocketId == pocketId }
            .filter {
                if (archived) {
                    it.isArchived
                } else {
                    TransactionVisibility.includes(it)
                }
            }
            .sortedWith(compareBy<TransactionRecord> { it.transactionDate }.thenBy { it.createdAt }.thenBy { it.id })

    fun all(): List<TransactionRecord> = values.values.toList()

    suspend fun save(transaction: TransactionRecord): TransactionRecord {
        if (
            values.values.any {
                it.recurringTransactionId == transaction.recurringTransactionId &&
                    it.transactionDate == transaction.transactionDate &&
                    it.parentTransactionId == null &&
                    transaction.parentTransactionId == null &&
                    transaction.recurringTransactionId != null
            }
        ) {
            throw IllegalStateException("duplicate recurring root occurrence")
        }
        values[transaction.id] = transaction
        return transaction
    }

    suspend fun update(transaction: TransactionRecord): TransactionRecord {
        values[transaction.id] = transaction
        return transaction
    }

    private fun TransactionModel.toDomain(fallbackId: String): TransactionRecord {
        return TransactionRecord(
            id = id ?: fallbackId,
            accountId = accountId,
            type = type,
            status = status,
            transactionDate = transactionDate,
            amount = amount,
            currencyCode = currencyCode,
            exchangeRate = exchangeRate,
            destinationAmount = destinationAmount,
            description = description,
            partnerId = partnerId,
            pocketId = pocketId,
            sourcePocketId = sourcePocketId,
            destinationPocketId = destinationPocketId,
            parentTransactionId = parentTransactionId,
            recurringTransactionId = recurringTransactionId,
            modifiedById = modifiedById,
            transactionOrigin = transactionOrigin,
            isArchived = isArchived,
            createdAt = createdAt,
        )
    }
}
