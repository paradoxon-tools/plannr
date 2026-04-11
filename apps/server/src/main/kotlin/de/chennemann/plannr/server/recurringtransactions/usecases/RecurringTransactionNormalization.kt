package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.common.domain.RecurrenceType
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.recurrence.domain.RecurrenceCalculator
import de.chennemann.plannr.server.recurrence.domain.RecurrencePattern
import java.time.DayOfWeek
import java.time.LocalDate
import org.springframework.stereotype.Component

@Component
class RecurringTransactionNormalization(
    private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculator(),
) {
    fun normalize(command: Fields): NormalizedFields {
        val firstOccurrenceDate = parseDate(command.firstOccurrenceDate, "Recurring transaction first occurrence date must be a plain date")
        val explicitFinalOccurrenceDate = command.finalOccurrenceDate?.trim()?.takeIf { it.isNotBlank() }
            ?.let { parseDate(it, "Recurring transaction final occurrence date must be a plain date") }
        val recurrenceType = runCatching { RecurrenceType.valueOf(command.recurrenceType.trim().uppercase()) }
            .getOrElse { throw ValidationException("validation_error", "Recurring transaction recurrence type is invalid") }

        val pattern = RecurrencePattern(
            firstOccurrenceDate = firstOccurrenceDate,
            finalOccurrenceDate = explicitFinalOccurrenceDate,
            recurrenceType = recurrenceType,
            skipCount = command.skipCount,
            daysOfWeek = command.daysOfWeek?.map { DayOfWeek.valueOf(it.trim().uppercase()) },
            weeksOfMonth = command.weeksOfMonth,
            daysOfMonth = command.daysOfMonth,
            monthsOfYear = command.monthsOfYear,
        )
        val normalizedFinalDate = recurrenceCalculator.normalizeFinalOccurrenceDate(pattern, command.maxRecurrenceCount)

        return NormalizedFields(
            firstOccurrenceDate = firstOccurrenceDate.toString(),
            finalOccurrenceDate = normalizedFinalDate?.toString(),
        )
    }

    data class Fields(
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

    data class NormalizedFields(
        val firstOccurrenceDate: String,
        val finalOccurrenceDate: String?,
    )

    private fun parseDate(value: String, message: String): LocalDate =
        try {
            LocalDate.parse(value.trim())
        } catch (_: Exception) {
            throw ValidationException("validation_error", message)
        }
}
