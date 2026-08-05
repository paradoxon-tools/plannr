package de.chennemann.plannr.server.transactions.templates.api.dto

data class CreateTransactionTemplateVersionCommand(
    val amount: Long,
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
