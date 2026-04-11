package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.common.domain.RecurrenceType
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.recurrence.domain.RecurrenceCalculator
import de.chennemann.plannr.server.recurrence.domain.RecurrencePattern
import java.time.DayOfWeek
import java.time.LocalDate
import org.springframework.stereotype.Component

@Component
class RecurringVersioningService(
    private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculator(),
) {
    fun predecessorOccurrence(existing: de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransaction, newFirstOccurrenceDate: String): String {
        val startDate = LocalDate.parse(newFirstOccurrenceDate)
        if (!startDate.isAfter(LocalDate.parse(existing.firstOccurrenceDate))) {
            throw ValidationException("validation_error", "New recurring version must start after the current version start date")
        }
        val predecessor = recurrenceCalculator.occurrences(
            existing.toPattern(),
            endInclusive = startDate.minusDays(1),
        ).lastOrNull() ?: throw ValidationException(
            "validation_error",
            "New recurring version must have a predecessor occurrence before its first occurrence date",
        )
        return predecessor.toString()
    }

    private fun de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransaction.toPattern(): RecurrencePattern =
        RecurrencePattern(
            firstOccurrenceDate = LocalDate.parse(firstOccurrenceDate),
            finalOccurrenceDate = finalOccurrenceDate?.let(LocalDate::parse),
            recurrenceType = RecurrenceType.valueOf(recurrenceType),
            skipCount = skipCount,
            daysOfWeek = daysOfWeek?.map(DayOfWeek::valueOf),
            weeksOfMonth = weeksOfMonth,
            daysOfMonth = daysOfMonth,
            monthsOfYear = monthsOfYear,
        )
}
