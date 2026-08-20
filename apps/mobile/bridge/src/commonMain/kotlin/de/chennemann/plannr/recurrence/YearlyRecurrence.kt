package de.chennemann.plannr.recurrence

import de.chennemann.plannr.data.TransactionTemplate
import de.chennemann.plannr.datetime.isAfter
import de.chennemann.plannr.datetime.isAfterOrEqual
import de.chennemann.plannr.datetime.plusYears
import kotlinx.datetime.LocalDate

internal class YearlyRecurrence : RecurrenceCalculator() {
    override fun calculateDates(recurrencePattern: TransactionTemplate.RecurrencePattern): Sequence<LocalDate> = sequence {
        var current = adjustToValidMonthAndDay(recurrencePattern.referenceDate, recurrencePattern.monthsOfYear, recurrencePattern.daysOfMonth)

        while (recurrencePattern.finalOccurrenceDate?.isAfterOrEqual(current) ?: true)  {
            yield(current)
            var nextDate = current.plusYears(1 + recurrencePattern.skipCount)
            nextDate = adjustToValidMonthAndDay(nextDate, recurrencePattern.monthsOfYear, recurrencePattern.daysOfMonth)
            current = nextDate
        }
    }
}