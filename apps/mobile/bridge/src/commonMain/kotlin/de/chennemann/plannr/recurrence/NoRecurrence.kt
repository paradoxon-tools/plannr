package de.chennemann.plannr.recurrence

import de.chennemann.plannr.data.TransactionTemplate
import de.chennemann.plannr.datetime.isAfterOrEqual
import kotlinx.datetime.LocalDate

internal class NoRecurrence : RecurrenceCalculator() {
    override fun calculateDates(recurrencePattern: TransactionTemplate.RecurrencePattern): Sequence<LocalDate> = sequence {
        if (recurrencePattern.finalOccurrenceDate?.isAfterOrEqual(recurrencePattern.referenceDate) ?: true) {
            yield(recurrencePattern.referenceDate)
        }
    }
}