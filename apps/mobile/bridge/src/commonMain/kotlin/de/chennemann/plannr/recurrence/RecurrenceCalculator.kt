package de.chennemann.plannr.recurrence

import de.chennemann.plannr.data.RecurrenceType
import de.chennemann.plannr.data.TransactionTemplate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.number

/**
 * Sealed class hierarchy for different recurrence calculation strategies
 */
internal sealed class RecurrenceCalculator {
    abstract fun calculateDates(recurrencePattern: TransactionTemplate.RecurrencePattern): Sequence<LocalDate>

    companion object {
        operator fun invoke(recurrenceType: RecurrenceType): RecurrenceCalculator = when (recurrenceType) {
            RecurrenceType.NONE -> NoRecurrence()
            RecurrenceType.DAILY -> DailyRecurrence()
            RecurrenceType.WEEKLY -> WeeklyRecurrence()
            RecurrenceType.MONTHLY -> MonthlyRecurrence()
            RecurrenceType.YEARLY -> YearlyRecurrence()
        }
    }
}

/**
 * Calculate the number of days in a month
 */
internal fun daysInMonth(month: Month, year: Int): Int = when (month) {
    Month.FEBRUARY -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
    else -> 31
}

/**
 * Convert negative days to positive days (e.g., -1 becomes last day of month)
 */
internal fun convertToPositiveDays(days: List<Int>, daysInMonth: Int): List<Int> =
    days.map { day -> if (day < 0) daysInMonth + day + 1 else day }
        .filter { it <= daysInMonth && it > 0 }

/**
 * Find the next valid month in a list of months
 */
internal fun findNextValidMonth(
    currentMonth: Month,
    currentYear: Int,
    validMonths: List<Month>?
): Pair<Month, Int> {
    if (validMonths.isNullOrEmpty()) {
        return if (currentMonth == Month.DECEMBER) {
            Pair(Month.JANUARY, currentYear + 1)
        } else {
            Pair(Month(currentMonth.number + 1), currentYear)
        }
    }

    val nextMonths = validMonths.filter { it.ordinal > currentMonth.ordinal }
    return if (nextMonths.isNotEmpty()) {
        Pair(nextMonths.minByOrNull { it.ordinal }!!, currentYear)
    } else {
        Pair(validMonths.minByOrNull { it.ordinal }!!, currentYear + 1)
    }
}

/**
 * Adjust a date to a valid month and day based on constraints
 */
internal fun adjustToValidMonthAndDay(
    date: LocalDate,
    validMonths: List<Month>?,
    validDays: List<Int>?
): LocalDate {
    var result = date

    // Adjust to a valid month if needed
    if (!validMonths.isNullOrEmpty() && !validMonths.contains(result.month)) {
        val nextMonths = validMonths.filter { it.ordinal >= result.month.ordinal }
        if (nextMonths.isNotEmpty()) {
            val nextMonth = nextMonths.minByOrNull { it.ordinal }!!
            val daysInNextMonth = daysInMonth(nextMonth, result.year)
            val day = minOf(result.day, daysInNextMonth)
            result = LocalDate(result.year, nextMonth, day)
        } else {
            val firstMonth = validMonths.minByOrNull { it.ordinal }!!
            val daysInFirstMonth = daysInMonth(firstMonth, result.year + 1)
            val day = minOf(result.day, daysInFirstMonth)
            result = LocalDate(result.year + 1, firstMonth, day)
        }
    }

    // Adjust to a valid day if needed
    if (!validDays.isNullOrEmpty()) {
        val daysInCurrentMonth = daysInMonth(result.month, result.year)
        val positiveDays = convertToPositiveDays(validDays, daysInCurrentMonth)

        if (!positiveDays.contains(result.day)) {
            val nextDays = positiveDays.filter { it > result.day }
            if (nextDays.isNotEmpty()) {
                val nextDay = nextDays.minOrNull()!!
                result = LocalDate(result.year, result.month, nextDay)
            } else {
                val (nextMonth, nextYear) = findNextValidMonth(result.month, result.year, validMonths)
                val daysInNextMonth = daysInMonth(nextMonth, nextYear)
                val positiveDaysNextMonth = convertToPositiveDays(validDays, daysInNextMonth)
                val firstDay = positiveDaysNextMonth.minOrNull() ?: 1
                result = LocalDate(nextYear, nextMonth, firstDay)
            }
        }
    }

    return result
}