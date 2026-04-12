package de.chennemann.plannr.server.transactions.api

import de.chennemann.plannr.server.transactions.api.dto.CreateTransactionRequest
import de.chennemann.plannr.server.transactions.api.dto.ModifyRecurringOccurrenceRequest
import de.chennemann.plannr.server.transactions.api.dto.TransactionResponse
import de.chennemann.plannr.server.transactions.api.dto.UpdateTransactionRequest
import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.usecases.CreateTransaction
import de.chennemann.plannr.server.transactions.usecases.ModifyRecurringOccurrence
import de.chennemann.plannr.server.transactions.usecases.UpdateTransaction

internal fun CreateTransactionRequest.toCommand(): CreateTransaction.Command {
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

internal fun UpdateTransactionRequest.toCommand(id: String): UpdateTransaction.Command {
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

internal fun ModifyRecurringOccurrenceRequest.toCommand(transactionId: String): ModifyRecurringOccurrence.Command {
    val normalizedType = type.trim().uppercase()
    return ModifyRecurringOccurrence.Command(
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

internal fun TransactionRecord.toResponse(): TransactionResponse =
    TransactionResponse(
        id = id,
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
