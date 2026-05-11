package de.chennemann.plannr.server.transactions.templates.api.dto

data class TransactionTemplateResponse(
    val id: Long,
    val sourcePocketId: Long?,
    val destinationPocketId: Long?,
    val partnerId: Long?,
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
    val previousVersionId: Long?,
    val isArchived: Boolean,
    val createdAt: Long,
) {
    val archived: Boolean
        get() = isArchived
}
