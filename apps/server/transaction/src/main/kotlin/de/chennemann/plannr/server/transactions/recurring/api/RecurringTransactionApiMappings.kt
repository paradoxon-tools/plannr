package de.chennemann.plannr.server.transactions.recurring.api

import de.chennemann.plannr.server.transactions.recurring.api.dto.CreateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.api.dto.RecurringTransactionResponse
import de.chennemann.plannr.server.transactions.recurring.api.dto.UpdateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction
import de.chennemann.plannr.server.transactions.recurring.service.RecurringTransactionService

fun CreateRecurringTransactionRequest.toCreateCommand() = RecurringTransactionService.CreateCommand(
    contractId, sourcePocketId, destinationPocketId, partnerId, title, description, amount, currencyCode,
    transactionType, firstOccurrenceDate, finalOccurrenceDate, recurrenceType, skipCount, daysOfWeek,
    weeksOfMonth, daysOfMonth, monthsOfYear, maxRecurrenceCount,
)

fun UpdateRecurringTransactionRequest.toUpdateCommand(id: String) = RecurringTransactionService.UpdateCommand(
    id, updateMode, contractId, sourcePocketId, destinationPocketId, partnerId, title,
    description, amount, currencyCode, transactionType, firstOccurrenceDate, finalOccurrenceDate,
    recurrenceType, skipCount, daysOfWeek, weeksOfMonth, daysOfMonth, monthsOfYear, maxRecurrenceCount,
)

fun RecurringTransaction.toResponse() = RecurringTransactionResponse(
    id,
    contractId,
    accountId,
    sourcePocketId,
    destinationPocketId,
    partnerId,
    title,
    description,
    amount,
    currencyCode,
    transactionType,
    firstOccurrenceDate,
    finalOccurrenceDate,
    recurrenceType,
    skipCount,
    daysOfWeek,
    weeksOfMonth,
    daysOfMonth,
    monthsOfYear,
    lastMaterializedDate,
    previousVersionId,
    isArchived,
    createdAt,
)

