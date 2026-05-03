package de.chennemann.plannr.server.transactions.api

import de.chennemann.plannr.server.transactions.api.dto.CreateTransactionRequest
import de.chennemann.plannr.server.transactions.api.dto.ModifyRecurringOccurrenceRequest
import de.chennemann.plannr.server.transactions.api.dto.TransactionResponse
import de.chennemann.plannr.server.transactions.api.dto.UpdateTransactionRequest
import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.service.TransactionService

fun CreateTransactionRequest.toCreateCommand(): TransactionService.CreateCommand {
    val normalizedType = type.trim().uppercase()
    return TransactionService.CreateCommand(
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

fun UpdateTransactionRequest.toUpdateCommand(id: String): TransactionService.UpdateCommand {
    val normalizedType = type.trim().uppercase()
    return TransactionService.UpdateCommand(
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

fun ModifyRecurringOccurrenceRequest.toModifyRecurringOccurrenceCommand(transactionId: String): TransactionService.ModifyRecurringOccurrenceCommand {
    val normalizedType = type.trim().uppercase()
    return TransactionService.ModifyRecurringOccurrenceCommand(
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

fun TransactionRecord.toResponse(): TransactionResponse =
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

