package de.chennemann.plannr.recurrence

import de.chennemann.plannr.data.TransactionTemplate
import de.chennemann.plannr.datetime.isAfter
import de.chennemann.plannr.datetime.isBefore
import de.chennemann.plannr.isNeitherNullNorEmpty
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.number

internal class MonthlyRecurrence : RecurrenceCalculator() {
    override fun calculateDates(recurrencePattern: TransactionTemplate.RecurrencePattern): Sequence<LocalDate> = sequence {
        val monthIterator = MonthIterator(recurrencePattern.referenceDate, recurrencePattern)

        for (monthInfo in monthIterator) {
            val datesInMonth = getDatesForMonth(monthInfo, recurrencePattern, recurrencePattern.referenceDate)

            for (date in datesInMonth) {
                if (recurrencePattern.finalOccurrenceDate?.isBefore(date) ?: false) return@sequence
                yield(date)
            }
        }
    }

    private fun getDatesForMonth(
        monthInfo: MonthInfo,
        recurrencePattern: TransactionTemplate.RecurrencePattern,
        startDate: LocalDate
    ): List<LocalDate> {

        val daysInMonth = daysInMonth(monthInfo.month, monthInfo.year)

        // If no specific days are specified, use the same day as startDate
        val targetDays = if (recurrencePattern.daysOfMonth.isNeitherNullNorEmpty()) {
            convertToPositiveDays(recurrencePattern.daysOfMonth, daysInMonth)
        } else {
            // Use the day from startDate, but clamp it to valid range for this month
            listOf(minOf(startDate.day, daysInMonth))
        }

        return targetDays
            .map { day -> LocalDate(monthInfo.year, monthInfo.month, day) }
            .filter { date -> date >= startDate } // Only include dates on or after startDate
            .sorted()
    }
}

private data class MonthInfo(val year: Int, val month: Month)

private class MonthIterator(startDate: LocalDate, private val recurrencePattern: TransactionTemplate.RecurrencePattern) : Iterator<MonthInfo> {

    private var currentYear = startDate.year
    // If monthsOfYear is specified, use it to determine the current month
    // If not, use the startDate's month
    // This is important to handle the case where the startDate is on a day that is not included in the monthsOfYear
    // It also takes into account the case where the startDate is in an included month but it's after an included dayOfMonth
    private var currentMonth = if (recurrencePattern.monthsOfYear.isNeitherNullNorEmpty()) {
        if (recurrencePattern.monthsOfYear.contains(startDate.month) && recurrencePattern.daysOfMonth?.contains(startDate.day) == true) {
            startDate.month
        } else {
            val monthsOfYear = recurrencePattern.monthsOfYear
            monthsOfYear.firstOrNull { it.number > startDate.month.number } ?: run {
                currentYear++
                monthsOfYear.minBy { it.number }
            }
        }
    } else startDate.month


    private var isFirst = true

    override fun hasNext(): Boolean = true // Infinite sequence, controlled by constraints

    override fun next(): MonthInfo {
        when {
            isFirst -> isFirst = false // Use initial values as is
            else -> advance()
        }

        return MonthInfo(currentYear, currentMonth)
    }

    private fun advance() {
        if (recurrencePattern.monthsOfYear.isNullOrEmpty()) {
            // Skip months based on skipCount
            repeat((1 + recurrencePattern.skipCount).toInt()) {
                if (currentMonth == Month.DECEMBER) {
                    currentMonth = Month.JANUARY
                    currentYear++
                } else {
                    currentMonth = Month(currentMonth.number + 1)
                }
            }
        } else do {
            if (currentMonth == Month.DECEMBER) {
                currentMonth = Month.JANUARY
                currentYear += 1 + recurrencePattern.skipCount.toInt()
            } else {
                currentMonth = Month(currentMonth.number + 1)
            }
        } while (!recurrencePattern.monthsOfYear.contains(currentMonth))
    }
}