package de.chennemann.plannr.server.recurringtransactions.dto

import de.chennemann.plannr.server.recurringtransactions.usecases.UpdateRecurringTransaction

data class UpdateRecurringTransactionRequest(
    val updateMode: String,
    val contractId: String?,
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
    val maxRecurrenceCount: Int?,
) {
    fun toCommand(id: String) = UpdateRecurringTransaction.Command(
        id, updateMode, contractId, sourcePocketId, destinationPocketId, partnerId, title,
        description, amount, currencyCode, transactionType, firstOccurrenceDate, finalOccurrenceDate,
        recurrenceType, skipCount, daysOfWeek, weeksOfMonth, daysOfMonth, monthsOfYear, maxRecurrenceCount,
    )
}
