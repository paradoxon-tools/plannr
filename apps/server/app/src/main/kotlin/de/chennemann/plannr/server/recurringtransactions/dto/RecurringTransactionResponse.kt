package de.chennemann.plannr.server.recurringtransactions.dto

import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransaction

data class RecurringTransactionResponse(
    val id: String,
    val contractId: String?,
    val accountId: String,
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
) {
    companion object {
        fun from(recurringTransaction: RecurringTransaction) = RecurringTransactionResponse(
            recurringTransaction.id,
            recurringTransaction.contractId,
            recurringTransaction.accountId,
            recurringTransaction.sourcePocketId,
            recurringTransaction.destinationPocketId,
            recurringTransaction.partnerId,
            recurringTransaction.title,
            recurringTransaction.description,
            recurringTransaction.amount,
            recurringTransaction.currencyCode,
            recurringTransaction.transactionType,
            recurringTransaction.firstOccurrenceDate,
            recurringTransaction.finalOccurrenceDate,
            recurringTransaction.recurrenceType,
            recurringTransaction.skipCount,
            recurringTransaction.daysOfWeek,
            recurringTransaction.weeksOfMonth,
            recurringTransaction.daysOfMonth,
            recurringTransaction.monthsOfYear,
            recurringTransaction.lastMaterializedDate,
            recurringTransaction.previousVersionId,
            recurringTransaction.isArchived,
            recurringTransaction.createdAt,
        )
    }
}
