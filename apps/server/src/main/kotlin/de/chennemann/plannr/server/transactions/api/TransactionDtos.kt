package de.chennemann.plannr.server.transactions.api

import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.usecases.CreateTransaction
import de.chennemann.plannr.server.transactions.usecases.UpdateTransaction

data class CreateTransactionRequest(
    val type: String,
    val status: String,
    val transactionDate: String,
    val amount: Long,
    val currencyCode: String,
    val exchangeRate: String?,
    val destinationAmount: Long?,
    val description: String,
    val partnerId: String?,
    val pocketId: String?,
    val sourcePocketId: String?,
    val destinationPocketId: String?,
) {
    fun toCommand(): CreateTransaction.Command {
        val normalizedType = type.trim().uppercase()
        return CreateTransaction.Command(
            type = type,
            status = status,
            transactionDate = transactionDate,
            amount = amount,
            currencyCode = currencyCode,
            exchangeRate = exchangeRate,
            destinationAmount = destinationAmount,
            description = description,
            partnerId = partnerId,
            sourcePocketId = when (normalizedType) {
                "EXPENSE" -> pocketId ?: sourcePocketId
                else -> sourcePocketId
            },
            destinationPocketId = when (normalizedType) {
                "INCOME" -> pocketId ?: destinationPocketId
                else -> destinationPocketId
            },
        )
    }
}

data class UpdateTransactionRequest(
    val type: String,
    val status: String,
    val transactionDate: String,
    val amount: Long,
    val currencyCode: String,
    val exchangeRate: String?,
    val destinationAmount: Long?,
    val description: String,
    val partnerId: String?,
    val pocketId: String?,
    val sourcePocketId: String?,
    val destinationPocketId: String?,
) {
    fun toCommand(id: String): UpdateTransaction.Command {
        val normalizedType = type.trim().uppercase()
        return UpdateTransaction.Command(
            id = id,
            type = type,
            status = status,
            transactionDate = transactionDate,
            amount = amount,
            currencyCode = currencyCode,
            exchangeRate = exchangeRate,
            destinationAmount = destinationAmount,
            description = description,
            partnerId = partnerId,
            sourcePocketId = when (normalizedType) {
                "EXPENSE" -> pocketId ?: sourcePocketId
                else -> sourcePocketId
            },
            destinationPocketId = when (normalizedType) {
                "INCOME" -> pocketId ?: destinationPocketId
                else -> destinationPocketId
            },
        )
    }
}

data class ModifyRecurringOccurrenceRequest(
    val type: String,
    val status: String,
    val transactionDate: String,
    val amount: Long,
    val currencyCode: String,
    val exchangeRate: String?,
    val destinationAmount: Long?,
    val description: String,
    val partnerId: String?,
    val pocketId: String?,
    val sourcePocketId: String?,
    val destinationPocketId: String?,
) {
    fun toCommand(transactionId: String): de.chennemann.plannr.server.transactions.usecases.ModifyRecurringOccurrence.Command {
        val normalizedType = type.trim().uppercase()
        return de.chennemann.plannr.server.transactions.usecases.ModifyRecurringOccurrence.Command(
            transactionId = transactionId,
            type = type,
            status = status,
            transactionDate = transactionDate,
            amount = amount,
            currencyCode = currencyCode,
            exchangeRate = exchangeRate,
            destinationAmount = destinationAmount,
            description = description,
            partnerId = partnerId,
            sourcePocketId = when (normalizedType) {
                "EXPENSE" -> pocketId ?: sourcePocketId
                else -> sourcePocketId
            },
            destinationPocketId = when (normalizedType) {
                "INCOME" -> pocketId ?: destinationPocketId
                else -> destinationPocketId
            },
        )
    }
}

data class TransactionResponse(
    val id: String,
    val accountId: String,
    val type: String,
    val status: String,
    val transactionDate: String,
    val amount: Long,
    val currencyCode: String,
    val exchangeRate: String?,
    val destinationAmount: Long?,
    val description: String,
    val partnerId: String?,
    val pocketId: String?,
    val sourcePocketId: String?,
    val destinationPocketId: String?,
    val parentTransactionId: String?,
    val recurringTransactionId: String?,
    val modifiedById: String?,
    val transactionOrigin: String,
    val isArchived: Boolean,
    val createdAt: Long,
) {
    companion object {
        fun from(transaction: TransactionRecord): TransactionResponse = TransactionResponse(
            id = transaction.id,
            accountId = transaction.accountId,
            type = transaction.type,
            status = transaction.status,
            transactionDate = transaction.transactionDate,
            amount = transaction.amount,
            currencyCode = transaction.currencyCode,
            exchangeRate = transaction.exchangeRate,
            destinationAmount = transaction.destinationAmount,
            description = transaction.description,
            partnerId = transaction.partnerId,
            pocketId = transaction.pocketId,
            sourcePocketId = transaction.sourcePocketId,
            destinationPocketId = transaction.destinationPocketId,
            parentTransactionId = transaction.parentTransactionId,
            recurringTransactionId = transaction.recurringTransactionId,
            modifiedById = transaction.modifiedById,
            transactionOrigin = transaction.transactionOrigin,
            isArchived = transaction.isArchived,
            createdAt = transaction.createdAt,
        )
    }
}
