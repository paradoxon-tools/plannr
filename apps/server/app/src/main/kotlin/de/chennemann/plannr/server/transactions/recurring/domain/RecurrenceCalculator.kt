package de.chennemann.plannr.server.transactions.recurring.domain

import de.chennemann.plannr.server.common.domain.RecurrenceType
import de.chennemann.plannr.server.common.error.ValidationException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.absoluteValue

class RecurrenceCalculator {
    fun occurrences(
        pattern: RecurrencePattern,
        limit: Int? = null,
        endInclusive: LocalDate? = pattern.finalOccurrenceDate,
    ): List<LocalDate> {
        if (pattern.skipCount < 0) {
            throw ValidationException("validation_error", "Recurring transaction skip count must not be negative")
        }
        val effectiveEnd = listOfNotNull(pattern.finalOccurrenceDate, endInclusive).minOrNull()
        if (effectiveEnd != null && effectiveEnd.isBefore(pattern.firstOccurrenceDate)) {
            return emptyList()
        }
        val results = when (pattern.recurrenceType) {
            RecurrenceType.NONE -> NoRecurrence().occurrences(pattern, effectiveEnd)
            RecurrenceType.DAILY -> DailyRecurrence().occurrences(pattern, effectiveEnd)
            RecurrenceType.WEEKLY -> WeeklyRecurrence().occurrences(pattern, effectiveEnd)
            RecurrenceType.MONTHLY -> MonthlyRecurrence().occurrences(pattern, effectiveEnd)
            RecurrenceType.YEARLY -> YearlyRecurrence().occurrences(pattern, effectiveEnd)
        }
        return limit?.let { results.take(it) } ?: results
    }

    fun normalizeFinalOccurrenceDate(pattern: RecurrencePattern, maxRecurrenceCount: Int?): LocalDate? {
        val normalizedCount = maxRecurrenceCount ?: return pattern.finalOccurrenceDate
        if (normalizedCount <= 0) {
            throw ValidationException("validation_error", "maxRecurrenceCount must be greater than 0")
        }
        val explicitEnd = pattern.finalOccurrenceDate
        val byCount = occurrences(pattern.copy(finalOccurrenceDate = null), limit = normalizedCount).lastOrNull()
        return listOfNotNull(explicitEnd, byCount).minOrNull()
    }
}

interface RecurrenceRule {
    fun occurrences(pattern: RecurrencePattern, endInclusive: LocalDate?): List<LocalDate>
}

class NoRecurrence : RecurrenceRule {
    override fun occurrences(pattern: RecurrencePattern, endInclusive: LocalDate?): List<LocalDate> =
        if (endInclusive != null && pattern.firstOccurrenceDate.isAfter(endInclusive)) emptyList() else listOf(pattern.firstOccurrenceDate)
}

class DailyRecurrence : RecurrenceRule {
    override fun occurrences(pattern: RecurrencePattern, endInclusive: LocalDate?): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        val step = pattern.skipCount + 1L
        val acceptedWeekdays = pattern.daysOfWeek?.toSet()
        var current = pattern.firstOccurrenceDate
        val lastDate = endInclusive ?: pattern.firstOccurrenceDate.plusYears(20)
        while (!current.isAfter(lastDate)) {
            if (acceptedWeekdays == null || current.dayOfWeek in acceptedWeekdays) {
                result += current
            }
            current = current.plusDays(step)
        }
        return result
    }
}

class WeeklyRecurrence : RecurrenceRule {
    override fun occurrences(pattern: RecurrencePattern, endInclusive: LocalDate?): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        val acceptedWeekdays = (pattern.daysOfWeek ?: listOf(pattern.firstOccurrenceDate.dayOfWeek)).sortedBy { it.value }
        val stepWeeks = pattern.skipCount + 1L
        val startWeek = pattern.firstOccurrenceDate.with(DayOfWeek.MONDAY)
        val lastDate = endInclusive ?: pattern.firstOccurrenceDate.plusYears(20)
        var currentWeek = startWeek
        while (!currentWeek.isAfter(lastDate)) {
            acceptedWeekdays.forEach { dayOfWeek ->
                val candidate = currentWeek.with(dayOfWeek)
                if (!candidate.isBefore(pattern.firstOccurrenceDate) && !candidate.isAfter(lastDate)) {
                    result += candidate
                }
            }
            currentWeek = currentWeek.plusWeeks(stepWeeks)
        }
        return result.sorted()
    }
}

class MonthlyRecurrence : RecurrenceRule {
    override fun occurrences(pattern: RecurrencePattern, endInclusive: LocalDate?): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        val lastDate = endInclusive ?: pattern.firstOccurrenceDate.plusYears(50)
        var monthCursor = YearMonth.from(pattern.firstOccurrenceDate)
        val stepMonths = pattern.skipCount + 1L
        while (!monthCursor.atEndOfMonth().isAfter(lastDate)) {
            if (monthAllowed(monthCursor, pattern)) {
                result += candidatesForMonth(pattern, monthCursor)
                    .filter { !it.isBefore(pattern.firstOccurrenceDate) && !it.isAfter(lastDate) }
            }
            monthCursor = monthCursor.plusMonths(stepMonths)
        }
        return result.distinct().sorted()
    }
}

class YearlyRecurrence : RecurrenceRule {
    override fun occurrences(pattern: RecurrencePattern, endInclusive: LocalDate?): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        val lastDate = endInclusive ?: pattern.firstOccurrenceDate.plusYears(100)
        val allowedMonths = pattern.monthsOfYear ?: listOf(pattern.firstOccurrenceDate.monthValue)
        var year = pattern.firstOccurrenceDate.year
        val stepYears = pattern.skipCount + 1
        while (LocalDate.of(year, 1, 1).isBefore(lastDate.plusDays(1))) {
            allowedMonths.sorted().forEach { month ->
                val yearMonth = YearMonth.of(year, month)
                result += candidatesForMonth(pattern, yearMonth)
                    .filter { !it.isBefore(pattern.firstOccurrenceDate) && !it.isAfter(lastDate) }
            }
            year += stepYears
        }
        return result.distinct().sorted()
    }
}

private fun monthAllowed(month: YearMonth, pattern: RecurrencePattern): Boolean =
    pattern.monthsOfYear?.contains(month.monthValue) ?: true

private fun candidatesForMonth(pattern: RecurrencePattern, month: YearMonth): List<LocalDate> {
    val byDayOfMonth = when {
        pattern.daysOfMonth != null -> pattern.daysOfMonth
            .mapNotNull { selector -> selectDayOfMonth(month, selector) }
        pattern.weeksOfMonth != null && pattern.daysOfWeek != null -> emptyList()
        else -> listOf(pattern.firstOccurrenceDate.dayOfMonth)
            .mapNotNull { selector -> selectDayOfMonth(month, selector) }
    }

    val byWeekAndWeekday = if (pattern.weeksOfMonth != null && pattern.daysOfWeek != null) {
        pattern.weeksOfMonth.flatMap { week ->
            pattern.daysOfWeek.mapNotNull { day -> selectWeekdayOfMonth(month, week, day) }
        }
    } else {
        emptyList()
    }

    return (byDayOfMonth + byWeekAndWeekday).distinct().sorted()
}

private fun selectDayOfMonth(month: YearMonth, selector: Int): LocalDate? {
    val maxDay = month.lengthOfMonth()
    val day = if (selector > 0) selector.coerceAtMost(maxDay) else (maxDay + selector + 1)
    return if (day in 1..maxDay) month.atDay(day) else null
}

private fun selectWeekdayOfMonth(month: YearMonth, weekSelector: Int, dayOfWeek: DayOfWeek): LocalDate? {
    val matchingDays = (1..month.lengthOfMonth())
        .map { month.atDay(it) }
        .filter { it.dayOfWeek == dayOfWeek }
    if (matchingDays.isEmpty()) return null
    val index = if (weekSelector > 0) weekSelector - 1 else matchingDays.size - weekSelector.absoluteValue
    return matchingDays.getOrNull(index)
}
