package de.chennemann.plannr.recurrence

import de.chennemann.plannr.data.TransactionTemplate
import de.chennemann.plannr.datetime.isAfter
import de.chennemann.plannr.datetime.isAfterOrEqual
import de.chennemann.plannr.datetime.plusDays
import kotlinx.datetime.LocalDate

internal class DailyRecurrence : RecurrenceCalculator() {
    override fun calculateDates(recurrencePattern: TransactionTemplate.RecurrencePattern): Sequence<LocalDate> = sequence {
        var current = recurrencePattern.referenceDate
        while (recurrencePattern.finalOccurrenceDate?.isAfterOrEqual(current) ?: true)  {
            yield(current)
            current = current.plusDays(1 + recurrencePattern.skipCount)
        }
    }
}