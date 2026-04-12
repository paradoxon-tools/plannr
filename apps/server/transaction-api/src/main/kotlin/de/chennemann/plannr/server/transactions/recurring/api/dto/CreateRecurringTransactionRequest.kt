package de.chennemann.plannr.server.transactions.recurring.api.dto

data class CreateRecurringTransactionRequest(
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
)
