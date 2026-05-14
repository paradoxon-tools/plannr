package de.chennemann.plannr.server.transactions.templates.persistence

import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.domain.RecurrencePattern
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("transaction_templates")
data class TransactionTemplateModel(
    @Id
    val id: Long?,
    @Column("source_pocket_id")
    val sourcePocketId: Long?,
    @Column("destination_pocket_id")
    val destinationPocketId: Long?,
    @Column("partner_id")
    val partnerId: Long?,
    val title: String,
    val description: String?,
    val amount: Long,
    @Column("currency_code")
    val currencyCode: String,
    @Column("transaction_type")
    val transactionType: String,
    @Column("first_occurrence_date")
    val firstOccurrenceDate: String,
    @Column("final_occurrence_date")
    val finalOccurrenceDate: String?,
    @Column("recurrence_type")
    val recurrenceType: String,
    @Column("skip_count")
    val skipCount: Int,
    @Column("days_of_week")
    val daysOfWeek: String?,
    @Column("weeks_of_month")
    val weeksOfMonth: String?,
    @Column("days_of_month")
    val daysOfMonth: String?,
    @Column("months_of_year")
    val monthsOfYear: String?,
    @Column("previous_version_id")
    val previousVersionId: Long?,
    @Column("is_archived")
    val isArchived: Boolean,
    @Column("created_at")
    val createdAt: Long,
)

fun TransactionTemplateModel.toDomain(): TransactionTemplate =
    TransactionTemplate(
        id = requireNotNull(id) { "TransactionTemplateModel.id must not be null when mapping to domain" },
        sourcePocketId = sourcePocketId,
        destinationPocketId = destinationPocketId,
        partnerId = partnerId,
        title = title,
        description = description,
        amount = amount,
        currencyCode = currencyCode,
        transactionType = transactionType,
        recurrencePattern = RecurrencePattern(
            firstOccurrenceDate = firstOccurrenceDate,
            finalOccurrenceDate = finalOccurrenceDate,
            recurrenceType = recurrenceType,
            skipCount = skipCount,
            daysOfWeek = daysOfWeek.toStringList(),
            weeksOfMonth = weeksOfMonth.toIntList(),
            daysOfMonth = daysOfMonth.toIntList(),
            monthsOfYear = monthsOfYear.toIntList(),
        ),
        previousVersionId = previousVersionId,
        isArchived = isArchived,
        createdAt = createdAt,
    )

fun TransactionTemplate.toModel(): TransactionTemplateModel =
    TransactionTemplateModel(
        id = id,
        sourcePocketId = sourcePocketId,
        destinationPocketId = destinationPocketId,
        partnerId = partnerId,
        title = title,
        description = description,
        amount = amount,
        currencyCode = currencyCode,
        transactionType = transactionType,
        firstOccurrenceDate = recurrencePattern.firstOccurrenceDate,
        finalOccurrenceDate = recurrencePattern.finalOccurrenceDate,
        recurrenceType = recurrencePattern.recurrenceType,
        skipCount = recurrencePattern.skipCount,
        daysOfWeek = recurrencePattern.daysOfWeek.toCsv(),
        weeksOfMonth = recurrencePattern.weeksOfMonth.toCsv(),
        daysOfMonth = recurrencePattern.daysOfMonth.toCsv(),
        monthsOfYear = recurrencePattern.monthsOfYear.toCsv(),
        previousVersionId = previousVersionId,
        isArchived = isArchived,
        createdAt = createdAt,
    )

private fun List<*>?.toCsv(): String? =
    this?.joinToString(",")?.takeIf { it.isNotBlank() }

private fun String?.toStringList(): List<String>? =
    this?.split(',')?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }

private fun String?.toIntList(): List<Int>? =
    toStringList()?.map(String::toInt)
