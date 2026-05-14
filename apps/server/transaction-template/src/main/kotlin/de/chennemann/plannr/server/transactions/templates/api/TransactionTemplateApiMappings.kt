package de.chennemann.plannr.server.transactions.templates.api

import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateRequest
import de.chennemann.plannr.server.transactions.templates.api.dto.TransactionTemplateResponse
import de.chennemann.plannr.server.transactions.templates.api.dto.UpdateTransactionTemplateRequest
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService

fun CreateTransactionTemplateRequest.toCreateCommand() = TransactionTemplateService.CreateCommand(
    sourcePocketId, destinationPocketId, partnerId, title, description, amount, currencyCode,
    transactionType, firstOccurrenceDate, finalOccurrenceDate, recurrenceType, skipCount, daysOfWeek,
    weeksOfMonth, daysOfMonth, monthsOfYear, maxRecurrenceCount,
)

fun UpdateTransactionTemplateRequest.toUpdateCommand(id: Long) = TransactionTemplateService.UpdateCommand(
    id, sourcePocketId, destinationPocketId, partnerId, title,
    description, amount, currencyCode, transactionType, firstOccurrenceDate, finalOccurrenceDate,
    recurrenceType, skipCount, daysOfWeek, weeksOfMonth, daysOfMonth, monthsOfYear, maxRecurrenceCount,
)

fun TransactionTemplate.toResponse() = recurrencePattern.let { pattern ->
    TransactionTemplateResponse(
        id,
        sourcePocketId,
        destinationPocketId,
        partnerId,
        title,
        description,
        amount,
        currencyCode,
        transactionType,
        pattern.firstOccurrenceDate,
        pattern.finalOccurrenceDate,
        pattern.recurrenceType,
        pattern.skipCount,
        pattern.daysOfWeek,
        pattern.weeksOfMonth,
        pattern.daysOfMonth,
        pattern.monthsOfYear,
        previousVersionId,
        isArchived,
        createdAt,
    )
}

