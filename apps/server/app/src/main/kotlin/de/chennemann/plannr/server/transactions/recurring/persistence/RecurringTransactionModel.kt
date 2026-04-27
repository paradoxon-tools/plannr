package de.chennemann.plannr.server.transactions.recurring.persistence

import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction

data class RecurringTransactionModel(
    val id: String?,
    val sourcePocketId: String?,
    val destinationPocketId: String?,
    val partnerId: String?,
    val title: String,
    val description: String?,
    val amount: Long,
    val currencyCode: String,
    val transactionType: String,
    val firstOccurrenceDate: String,
    val finalOccurrenceDate: String?,
    val recurrenceType: String,
    val skipCount: Int,
    val daysOfWeek: List<String>?,
    val weeksOfMonth: List<Int>?,
    val daysOfMonth: List<Int>?,
    val monthsOfYear: List<Int>?,
    val lastMaterializedDate: String?,
    val previousVersionId: String?,
    val isArchived: Boolean,
    val createdAt: Long,
)

internal fun RecurringTransaction.toModel(): RecurringTransactionModel =
    RecurringTransactionModel(
        id = id,
        sourcePocketId = sourcePocketId,
        destinationPocketId = destinationPocketId,
        partnerId = partnerId,
        title = title,
        description = description,
        amount = amount,
        currencyCode = currencyCode,
        transactionType = transactionType,
        firstOccurrenceDate = firstOccurrenceDate,
        finalOccurrenceDate = finalOccurrenceDate,
        recurrenceType = recurrenceType,
        skipCount = skipCount,
        daysOfWeek = daysOfWeek,
        weeksOfMonth = weeksOfMonth,
        daysOfMonth = daysOfMonth,
        monthsOfYear = monthsOfYear,
        lastMaterializedDate = lastMaterializedDate,
        previousVersionId = previousVersionId,
        isArchived = isArchived,
        createdAt = createdAt,
    )
