package de.chennemann.plannr.recurrence

import de.chennemann.plannr.data.TransactionTemplate
import de.chennemann.plannr.datetime.isAfter
import de.chennemann.plannr.datetime.isAfterOrEqual
import de.chennemann.plannr.datetime.isBefore
import de.chennemann.plannr.datetime.plusDays
import de.chennemann.plannr.datetime.plusWeeks
import de.chennemann.plannr.isNeitherNullNorEmpty
import kotlinx.datetime.LocalDate

internal class WeeklyRecurrence : RecurrenceCalculator() {
    override fun calculateDates(recurrencePattern: TransactionTemplate.RecurrencePattern): Sequence<LocalDate> = sequence {
        var current = recurrencePattern.referenceDate

        // Find first valid day of week if specified
        if (recurrencePattern.daysOfWeek.isNeitherNullNorEmpty()) {
            while (!recurrencePattern.daysOfWeek.contains(current.dayOfWeek)) {
                current = current.plusDays(1)
                if (recurrencePattern.finalOccurrenceDate?.isBefore(current) ?: false) return@sequence
            }
        }

        val daysInCurrentWeek = mutableListOf<LocalDate>()

        while (recurrencePattern.finalOccurrenceDate?.isAfterOrEqual(current) ?: true) {
            if (recurrencePattern.daysOfWeek.isNeitherNullNorEmpty()) {
                daysInCurrentWeek.add(current)
            }

            yield(current)

            current = if (recurrencePattern.daysOfWeek.isNeitherNullNorEmpty()) {
                findNextWeeklyDate(current, recurrencePattern, daysInCurrentWeek)
            } else {
                current.plusWeeks(1 + recurrencePattern.skipCount)
            }
        }
    }

    private fun findNextWeeklyDate(
        current: LocalDate,
        recurrencePattern: TransactionTemplate.RecurrencePattern,
        daysInCurrentWeek: MutableList<LocalDate>
    ): LocalDate {
        var next = current.plusDays(1)

        // Find next valid day of week
        while (!recurrencePattern.daysOfWeek!!.contains(next.dayOfWeek)) {
            next = next.plusDays(1)
        }

        // Check if we've moved to next week
        val firstDayOfWeek = daysInCurrentWeek.first()
        val weeksSinceFirst = (next.toEpochDays() - firstDayOfWeek.toEpochDays()) / 7

        if (weeksSinceFirst > 0) {
            if (recurrencePattern.skipCount > 0) {
                next = next.plusWeeks(recurrencePattern.skipCount)
            }
            daysInCurrentWeek.clear()
        }

        return next
    }
}