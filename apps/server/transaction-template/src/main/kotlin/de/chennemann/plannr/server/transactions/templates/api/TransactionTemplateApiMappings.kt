package de.chennemann.plannr.server.transactions.templates.api

import de.chennemann.plannr.server.transactions.templates.api.dto.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.api.dto.TransactionTemplateVersion
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate as DomainTemplate
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplateVersion as DomainVersion

fun DomainTemplate.toDTO() = TransactionTemplate(
    id, contractId, sourcePocketId, destinationPocketId, financialProfileId, partnerId,
    title, description, currencyCode, transactionType, versions.map(DomainVersion::toDTO), isArchived, createdAt,
)

private fun DomainVersion.toDTO() = TransactionTemplateVersion(
    id, transactionTemplateId, amount, recurrencePattern.firstOccurrenceDate,
    recurrencePattern.finalOccurrenceDate, recurrencePattern.recurrenceType, recurrencePattern.skipCount,
    recurrencePattern.daysOfWeek, recurrencePattern.weeksOfMonth, recurrencePattern.daysOfMonth,
    recurrencePattern.monthsOfYear, validFrom, validUntil, createdAt,
)
