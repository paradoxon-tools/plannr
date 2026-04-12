package de.chennemann.plannr.server.transactions.recurring.api

import de.chennemann.plannr.server.transactions.recurring.api.dto.CreateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.api.dto.RecurringTransactionResponse
import de.chennemann.plannr.server.transactions.recurring.api.dto.UpdateRecurringTransactionRequest
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction
import de.chennemann.plannr.server.transactions.recurring.usecases.CreateRecurringTransaction
import de.chennemann.plannr.server.transactions.recurring.usecases.UpdateRecurringTransaction

internal fun CreateRecurringTransactionRequest.toCommand() = CreateRecurringTransaction.Command(
    contractId, sourcePocketId, destinationPocketId, partnerId, title, description, amount, currencyCode,
    transactionType, firstOccurrenceDate, finalOccurrenceDate, recurrenceType, skipCount, daysOfWeek,
    weeksOfMonth, daysOfMonth, monthsOfYear, maxRecurrenceCount,
)

internal fun UpdateRecurringTransactionRequest.toCommand(id: String) = UpdateRecurringTransaction.Command(
    id, updateMode, contractId, sourcePocketId, destinationPocketId, partnerId, title,
    description, amount, currencyCode, transactionType, firstOccurrenceDate, finalOccurrenceDate,
    recurrenceType, skipCount, daysOfWeek, weeksOfMonth, daysOfMonth, monthsOfYear, maxRecurrenceCount,
)

internal fun RecurringTransaction.toResponse() = RecurringTransactionResponse(
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
