package de.chennemann.plannr.server.recurringtransactions.api

import de.chennemann.plannr.server.recurringtransactions.api.dto.CreateRecurringTransactionRequest
import de.chennemann.plannr.server.recurringtransactions.api.dto.RecurringTransactionResponse
import de.chennemann.plannr.server.recurringtransactions.api.dto.UpdateRecurringTransactionRequest
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.usecases.CreateRecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.usecases.UpdateRecurringTransaction

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
