package de.chennemann.plannr.server.transactions.templates.persistence

import de.chennemann.plannr.server.transactions.templates.domain.RecurrencePattern
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplateVersion
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("transaction_templates")
data class TransactionTemplateModel(
    @Id val id: Long?,
    @Column("contract_id") val contractId: Long?,
    @Column("source_pocket_id") val sourcePocketId: Long?,
    @Column("destination_pocket_id") val destinationPocketId: Long?,
    @Column("financial_profile_id") val financialProfileId: Long,
    @Column("partner_id") val partnerId: Long?,
    val title: String,
    val description: String?,
    @Column("currency_code") val currencyCode: String,
    @Column("transaction_type") val transactionType: String,
    @Column("is_archived") val isArchived: Boolean,
    @Column("created_at") val createdAt: Long,
)

@Table("transaction_template_versions")
data class TransactionTemplateVersionModel(
    @Id val id: Long?,
    @Column("transaction_template_id") val transactionTemplateId: Long,
    val amount: Long,
    @Column("first_occurrence_date") val firstOccurrenceDate: String,
    @Column("final_occurrence_date") val finalOccurrenceDate: String?,
    @Column("recurrence_type") val recurrenceType: String,
    @Column("skip_count") val skipCount: Int,
    @Column("days_of_week") val daysOfWeek: String?,
    @Column("weeks_of_month") val weeksOfMonth: String?,
    @Column("days_of_month") val daysOfMonth: String?,
    @Column("months_of_year") val monthsOfYear: String?,
    @Column("valid_from") val validFrom: String,
    @Column("valid_until") val validUntil: String?,
    @Column("created_at") val createdAt: Long,
)

fun TransactionTemplateVersionModel.toDomain() = TransactionTemplateVersion(
    requireNotNull(id), transactionTemplateId, amount,
    RecurrencePattern(
        firstOccurrenceDate, finalOccurrenceDate, recurrenceType, skipCount,
        daysOfWeek.toStringList(), weeksOfMonth.toIntList(), daysOfMonth.toIntList(), monthsOfYear.toIntList(),
    ),
    validFrom, validUntil, createdAt,
)

fun TransactionTemplateVersion.toModel() = TransactionTemplateVersionModel(
    id, transactionTemplateId, amount, recurrencePattern.firstOccurrenceDate,
    recurrencePattern.finalOccurrenceDate, recurrencePattern.recurrenceType, recurrencePattern.skipCount,
    recurrencePattern.daysOfWeek.toCsv(), recurrencePattern.weeksOfMonth.toCsv(),
    recurrencePattern.daysOfMonth.toCsv(), recurrencePattern.monthsOfYear.toCsv(),
    validFrom, validUntil, createdAt,
)

internal fun List<*>?.toCsv(): String? = this?.joinToString(",")?.takeIf(String::isNotBlank)
private fun String?.toStringList() = this?.split(',')?.filter(String::isNotBlank)?.takeIf(List<String>::isNotEmpty)
private fun String?.toIntList() = toStringList()?.map(String::toInt)
