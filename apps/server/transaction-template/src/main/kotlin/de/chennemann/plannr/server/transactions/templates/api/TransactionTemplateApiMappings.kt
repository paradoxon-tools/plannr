package de.chennemann.plannr.server.transactions.templates.api

import de.chennemann.plannr.server.transactions.templates.api.dto.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate as DomainTransactionTemplate

fun DomainTransactionTemplate.toDTO() = recurrencePattern.let { pattern ->
    TransactionTemplate(
        id = id,
        contractId = contractId,
        sourcePocketId = sourcePocketId,
        destinationPocketId = destinationPocketId,
        financialProfileId = financialProfileId,
        partnerId = partnerId,
        title = title,
        description = description,
        amount = amount,
        currencyCode = currencyCode,
        transactionType = transactionType,
        firstOccurrenceDate = pattern.firstOccurrenceDate,
        finalOccurrenceDate = pattern.finalOccurrenceDate,
        recurrenceType = pattern.recurrenceType,
        skipCount = pattern.skipCount,
        daysOfWeek = pattern.daysOfWeek,
        weeksOfMonth = pattern.weeksOfMonth,
        daysOfMonth = pattern.daysOfMonth,
        monthsOfYear = pattern.monthsOfYear,
        previousVersionId = previousVersionId,
        isArchived = isArchived,
        createdAt = createdAt,
    )
}

