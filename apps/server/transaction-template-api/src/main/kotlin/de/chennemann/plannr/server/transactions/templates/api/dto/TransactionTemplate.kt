package de.chennemann.plannr.server.transactions.templates.api.dto

data class TransactionTemplate(
    val id: Long,
    val contractId: Long?,
    val sourcePocketId: Long?,
    val destinationPocketId: Long?,
    val financialProfileId: Long,
    val partnerId: Long?,
    val title: String,
    val description: String?,
    val currencyCode: String,
    val transactionType: String,
    val versions: List<TransactionTemplateVersion>,
    val isArchived: Boolean,
    val createdAt: Long,
)

data class TransactionTemplateVersion(
    val id: Long,
    val transactionTemplateId: Long,
    val amount: Long,
    val firstOccurrenceDate: String,
    val finalOccurrenceDate: String?,
    val recurrenceType: String,
    val skipCount: Int,
    val daysOfWeek: List<String>?,
    val weeksOfMonth: List<Int>?,
    val daysOfMonth: List<Int>?,
    val monthsOfYear: List<Int>?,
    val validFrom: String,
    val validUntil: String?,
    val createdAt: Long,
)
