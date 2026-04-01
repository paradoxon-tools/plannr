package de.chennemann.plannr.server.recurringtransactions.domain

import de.chennemann.plannr.server.common.error.ValidationException
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class RecurringTransaction private constructor(
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
        operator fun invoke(
            id: String,
            contractId: String?,
            accountId: String,
            sourcePocketId: String?,
            destinationPocketId: String?,
            partnerId: String?,
            title: String,
            description: String?,
            amount: Long,
            currencyCode: String,
            transactionType: String,
            firstOccurrenceDate: String,
            finalOccurrenceDate: String?,
            recurrenceType: String,
            skipCount: Int,
            daysOfWeek: List<String>?,
            weeksOfMonth: List<Int>?,
            daysOfMonth: List<Int>?,
            monthsOfYear: List<Int>?,
            lastMaterializedDate: String?,
            previousVersionId: String?,
            isArchived: Boolean,
            createdAt: Long,
        ): RecurringTransaction {
            val normalizedId = id.trim()
            val normalizedContractId = contractId?.trim()?.takeIf { it.isNotBlank() }
            val normalizedAccountId = accountId.trim()
            val normalizedSourcePocketId = sourcePocketId?.trim()?.takeIf { it.isNotBlank() }
            val normalizedDestinationPocketId = destinationPocketId?.trim()?.takeIf { it.isNotBlank() }
            val normalizedPartnerId = partnerId?.trim()?.takeIf { it.isNotBlank() }
            val normalizedTitle = title.trim()
            val normalizedDescription = description?.trim()?.takeIf { it.isNotBlank() }
            val normalizedCurrencyCode = currencyCode.trim().uppercase()
            val normalizedTransactionType = transactionType.trim().lowercase()
            val normalizedFirstOccurrenceDate = firstOccurrenceDate.trim()
            val normalizedFinalOccurrenceDate = finalOccurrenceDate?.trim()?.takeIf { it.isNotBlank() }
            val normalizedRecurrenceType = recurrenceType.trim().lowercase()
            val normalizedDaysOfWeek = daysOfWeek?.map { it.trim().uppercase() }?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }
            val normalizedWeeksOfMonth = weeksOfMonth?.takeIf { it.isNotEmpty() }
            val normalizedDaysOfMonth = daysOfMonth?.takeIf { it.isNotEmpty() }
            val normalizedMonthsOfYear = monthsOfYear?.takeIf { it.isNotEmpty() }
            val normalizedLastMaterializedDate = lastMaterializedDate?.trim()?.takeIf { it.isNotBlank() }
            val normalizedPreviousVersionId = previousVersionId?.trim()?.takeIf { it.isNotBlank() }

            if (normalizedId.isBlank()) throw ValidationException("validation_error", "Recurring transaction id must not be blank")
            if (normalizedAccountId.isBlank()) throw ValidationException("validation_error", "Recurring transaction account id must not be blank")
            if (normalizedTitle.isBlank()) throw ValidationException("validation_error", "Recurring transaction title must not be blank")
            if (normalizedCurrencyCode.isBlank()) throw ValidationException("validation_error", "Recurring transaction currency code must not be blank")
            if (normalizedTransactionType.isBlank()) throw ValidationException("validation_error", "Recurring transaction type must not be blank")
            if (normalizedFirstOccurrenceDate.isBlank()) throw ValidationException("validation_error", "Recurring transaction first occurrence date must not be blank")
            if (normalizedRecurrenceType.isBlank()) throw ValidationException("validation_error", "Recurring transaction recurrence type must not be blank")
            if (amount < 0) throw ValidationException("validation_error", "Recurring transaction amount must not be negative")
            if (skipCount < 0) throw ValidationException("validation_error", "Recurring transaction skip count must not be negative")

            val parsedFirstDate = parseDate(normalizedFirstOccurrenceDate, "Recurring transaction first occurrence date must be a plain date")
            val parsedFinalDate = normalizedFinalOccurrenceDate?.let { parseDate(it, "Recurring transaction final occurrence date must be a plain date") }
            normalizedLastMaterializedDate?.let { parseDate(it, "Recurring transaction last materialized date must be a plain date") }
            if (parsedFinalDate != null && parsedFinalDate.isBefore(parsedFirstDate)) {
                throw ValidationException("validation_error", "Recurring transaction final occurrence date must not be before first occurrence date")
            }

            validateTransactionTypeCombination(
                transactionType = normalizedTransactionType,
                sourcePocketId = normalizedSourcePocketId,
                destinationPocketId = normalizedDestinationPocketId,
            )
            validateRecurrenceCombination(
                recurrenceType = normalizedRecurrenceType,
                skipCount = skipCount,
                daysOfWeek = normalizedDaysOfWeek,
                weeksOfMonth = normalizedWeeksOfMonth,
                daysOfMonth = normalizedDaysOfMonth,
                monthsOfYear = normalizedMonthsOfYear,
            )
            validateSelectorValueRanges(
                daysOfWeek = normalizedDaysOfWeek,
                weeksOfMonth = normalizedWeeksOfMonth,
                daysOfMonth = normalizedDaysOfMonth,
                monthsOfYear = normalizedMonthsOfYear,
            )

            return RecurringTransaction(
                id = normalizedId,
                contractId = normalizedContractId,
                accountId = normalizedAccountId,
                sourcePocketId = normalizedSourcePocketId,
                destinationPocketId = normalizedDestinationPocketId,
                partnerId = normalizedPartnerId,
                title = normalizedTitle,
                description = normalizedDescription,
                amount = amount,
                currencyCode = normalizedCurrencyCode,
                transactionType = normalizedTransactionType,
                firstOccurrenceDate = normalizedFirstOccurrenceDate,
                finalOccurrenceDate = normalizedFinalOccurrenceDate,
                recurrenceType = normalizedRecurrenceType,
                skipCount = skipCount,
                daysOfWeek = normalizedDaysOfWeek,
                weeksOfMonth = normalizedWeeksOfMonth,
                daysOfMonth = normalizedDaysOfMonth,
                monthsOfYear = normalizedMonthsOfYear,
                lastMaterializedDate = normalizedLastMaterializedDate,
                previousVersionId = normalizedPreviousVersionId,
                isArchived = isArchived,
                createdAt = createdAt,
            )
        }

        private fun parseDate(value: String, message: String): LocalDate =
            try { LocalDate.parse(value) } catch (_: DateTimeParseException) { throw ValidationException("validation_error", message) }

        private fun validateTransactionTypeCombination(
            transactionType: String,
            sourcePocketId: String?,
            destinationPocketId: String?,
        ) {
            when (transactionType) {
                "expense" -> {
                    if (sourcePocketId == null) {
                        throw ValidationException("validation_error", "Expense recurring transaction requires source pocket")
                    }
                    if (destinationPocketId != null) {
                        throw ValidationException("validation_error", "Expense recurring transaction must not define destination pocket")
                    }
                }
                "income" -> {
                    if (destinationPocketId == null) {
                        throw ValidationException("validation_error", "Income recurring transaction requires destination pocket")
                    }
                    if (sourcePocketId != null) {
                        throw ValidationException("validation_error", "Income recurring transaction must not define source pocket")
                    }
                }
                "transfer" -> {
                    if (sourcePocketId == null || destinationPocketId == null) {
                        throw ValidationException("validation_error", "Transfer recurring transaction requires source and destination pockets")
                    }
                    if (sourcePocketId == destinationPocketId) {
                        throw ValidationException("validation_error", "Transfer recurring transaction source and destination pockets must differ")
                    }
                }
                else -> throw ValidationException("validation_error", "Recurring transaction type is invalid")
            }
        }

        private fun validateRecurrenceCombination(
            recurrenceType: String,
            skipCount: Int,
            daysOfWeek: List<String>?,
            weeksOfMonth: List<Int>?,
            daysOfMonth: List<Int>?,
            monthsOfYear: List<Int>?,
        ) {
            when (recurrenceType) {
                "none" -> {
                    if (skipCount != 0 || daysOfWeek != null || weeksOfMonth != null || daysOfMonth != null || monthsOfYear != null) {
                        throw ValidationException("validation_error", "One-time recurring transactions must not define recurrence selectors or skip count")
                    }
                }
                "daily" -> {
                    if (weeksOfMonth != null || daysOfMonth != null || monthsOfYear != null) {
                        throw ValidationException("validation_error", "Daily recurring transactions only support daysOfWeek and skipCount")
                    }
                }
                "weekly" -> {
                    if (daysOfMonth != null || monthsOfYear != null) {
                        throw ValidationException("validation_error", "Weekly recurring transactions only support daysOfWeek, weeksOfMonth and skipCount")
                    }
                }
                "monthly" -> {
                    // weeksOfMonth, daysOfMonth, monthsOfYear, skipCount are allowed.
                }
                else -> throw ValidationException("validation_error", "Recurring transaction recurrence type is invalid")
            }
        }

        private fun validateSelectorValueRanges(
            daysOfWeek: List<String>?,
            weeksOfMonth: List<Int>?,
            daysOfMonth: List<Int>?,
            monthsOfYear: List<Int>?,
        ) {
            val validDaysOfWeek = setOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
            if (daysOfWeek != null && daysOfWeek.any { it !in validDaysOfWeek }) {
                throw ValidationException("validation_error", "Recurring transaction daysOfWeek contains invalid values")
            }
            if (weeksOfMonth != null && weeksOfMonth.any { it == 0 || it !in -5..5 }) {
                throw ValidationException("validation_error", "Recurring transaction weeksOfMonth must be between -5 and 5 and must not be 0")
            }
            if (daysOfMonth != null && daysOfMonth.any { it == 0 || it !in -31..31 }) {
                throw ValidationException("validation_error", "Recurring transaction daysOfMonth must be between -31 and 31 and must not be 0")
            }
            if (monthsOfYear != null && monthsOfYear.any { it !in 1..12 }) {
                throw ValidationException("validation_error", "Recurring transaction monthsOfYear must be between 1 and 12")
            }
        }
    }
}
