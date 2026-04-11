package de.chennemann.plannr.server.recurrence.domain

import de.chennemann.plannr.server.common.domain.RecurrenceType
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class RecurrenceCalculatorTest {
    private val calculator = RecurrenceCalculator()

    @Test
    fun `none emits only first date`() {
        assertEquals(
            listOf(LocalDate.parse("2024-01-10")),
            calculator.occurrences(pattern(recurrenceType = RecurrenceType.NONE, firstOccurrenceDate = "2024-01-10", finalOccurrenceDate = null)),
        )
    }

    @Test
    fun `daily recurrence honors skip count`() {
        assertEquals(
            listOf("2024-01-01", "2024-01-03", "2024-01-05").map(LocalDate::parse),
            calculator.occurrences(pattern(recurrenceType = RecurrenceType.DAILY, skipCount = 1, finalOccurrenceDate = "2024-01-05")),
        )
    }

    @Test
    fun `weekly recurrence supports multiple weekdays`() {
        assertEquals(
            listOf("2024-01-01", "2024-01-03", "2024-01-08", "2024-01-10").map(LocalDate::parse),
            calculator.occurrences(
                pattern(
                    recurrenceType = RecurrenceType.WEEKLY,
                    firstOccurrenceDate = "2024-01-01",
                    finalOccurrenceDate = "2024-01-10",
                    daysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                    weeksOfMonth = null,
                    daysOfMonth = null,
                    monthsOfYear = null,
                ),
            ),
        )
    }

    @Test
    fun `monthly recurrence supports negative day of month selectors`() {
        assertEquals(
            listOf("2024-01-31", "2024-02-29", "2024-03-31").map(LocalDate::parse),
            calculator.occurrences(
                pattern(
                    recurrenceType = RecurrenceType.MONTHLY,
                    firstOccurrenceDate = "2024-01-31",
                    finalOccurrenceDate = "2024-03-31",
                    daysOfMonth = listOf(-1),
                    daysOfWeek = null,
                    weeksOfMonth = null,
                    monthsOfYear = null,
                ),
            ),
        )
    }

    @Test
    fun `monthly recurrence supports week of month selectors`() {
        assertEquals(
            listOf("2024-01-08", "2024-02-12", "2024-03-11").map(LocalDate::parse),
            calculator.occurrences(
                pattern(
                    recurrenceType = RecurrenceType.MONTHLY,
                    firstOccurrenceDate = "2024-01-08",
                    finalOccurrenceDate = "2024-03-31",
                    daysOfWeek = listOf(DayOfWeek.MONDAY),
                    weeksOfMonth = listOf(2),
                    daysOfMonth = null,
                    monthsOfYear = null,
                ),
            ),
        )
    }

    @Test
    fun `yearly recurrence clamps leap day`() {
        assertEquals(
            listOf("2024-02-29", "2025-02-28", "2026-02-28").map(LocalDate::parse),
            calculator.occurrences(
                pattern(
                    recurrenceType = RecurrenceType.YEARLY,
                    firstOccurrenceDate = "2024-02-29",
                    finalOccurrenceDate = "2026-12-31",
                    daysOfWeek = null,
                    weeksOfMonth = null,
                    daysOfMonth = listOf(29),
                    monthsOfYear = listOf(2),
                ),
            ),
        )
    }

    @Test
    fun `max recurrence count normalizes final date`() {
        assertEquals(
            LocalDate.parse("2024-03-15"),
            calculator.normalizeFinalOccurrenceDate(
                pattern(
                    recurrenceType = RecurrenceType.MONTHLY,
                    firstOccurrenceDate = "2024-01-15",
                    finalOccurrenceDate = null,
                    daysOfMonth = listOf(15),
                    daysOfWeek = null,
                    weeksOfMonth = null,
                    monthsOfYear = null,
                ),
                maxRecurrenceCount = 3,
            ),
        )
    }

    private fun pattern(
        recurrenceType: RecurrenceType,
        firstOccurrenceDate: String = "2024-01-01",
        finalOccurrenceDate: String? = "2024-01-05",
        skipCount: Int = 0,
        daysOfWeek: List<DayOfWeek>? = listOf(DayOfWeek.MONDAY),
        weeksOfMonth: List<Int>? = null,
        daysOfMonth: List<Int>? = null,
        monthsOfYear: List<Int>? = null,
    ) = RecurrencePattern(
        firstOccurrenceDate = LocalDate.parse(firstOccurrenceDate),
        finalOccurrenceDate = finalOccurrenceDate?.let(LocalDate::parse),
        recurrenceType = recurrenceType,
        skipCount = skipCount,
        daysOfWeek = daysOfWeek,
        weeksOfMonth = weeksOfMonth,
        daysOfMonth = daysOfMonth,
        monthsOfYear = monthsOfYear,
    )
}
