package de.chennemann.plannr.server.recurrence.domain

import de.chennemann.plannr.server.common.domain.RecurrenceType
import de.chennemann.plannr.server.transactions.recurring.domain.RecurrenceCalculator
import de.chennemann.plannr.server.transactions.recurring.domain.RecurrencePattern
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
    fun `daily recurrence with skip count zero emits every day until final date`() {
        assertEquals(
            listOf("2024-01-01", "2024-01-02", "2024-01-03").map(LocalDate::parse),
            calculator.occurrences(pattern(recurrenceType = RecurrenceType.DAILY, skipCount = 0, finalOccurrenceDate = "2024-01-03")),
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
    fun `weekly recurrence defaults to first occurrence weekday and can skip whole weeks`() {
        assertEquals(
            listOf("2024-01-03", "2024-01-17", "2024-01-31").map(LocalDate::parse),
            calculator.occurrences(
                pattern(
                    recurrenceType = RecurrenceType.WEEKLY,
                    firstOccurrenceDate = "2024-01-03",
                    finalOccurrenceDate = "2024-01-31",
                    skipCount = 1,
                    daysOfWeek = null,
                    weeksOfMonth = null,
                    daysOfMonth = null,
                    monthsOfYear = null,
                ),
            ),
        )
    }

    @Test
    fun `weekly recurrence moves first emitted date forward to next matching weekday`() {
        assertEquals(
            listOf("2024-01-03", "2024-01-08", "2024-01-10").map(LocalDate::parse),
            calculator.occurrences(
                pattern(
                    recurrenceType = RecurrenceType.WEEKLY,
                    firstOccurrenceDate = "2024-01-02",
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
    fun `monthly recurrence defaults to first day of month selector and clamps short months`() {
        assertEquals(
            listOf("2024-01-31", "2024-02-29", "2024-03-31").map(LocalDate::parse),
            calculator.occurrences(
                pattern(
                    recurrenceType = RecurrenceType.MONTHLY,
                    firstOccurrenceDate = "2024-01-31",
                    finalOccurrenceDate = "2024-03-31",
                    daysOfMonth = null,
                    daysOfWeek = null,
                    weeksOfMonth = null,
                    monthsOfYear = null,
                ),
            ),
        )
    }

    @Test
    fun `monthly recurrence can restrict allowed months`() {
        assertEquals(
            listOf("2024-01-15", "2024-03-15", "2024-05-15").map(LocalDate::parse),
            calculator.occurrences(
                pattern(
                    recurrenceType = RecurrenceType.MONTHLY,
                    firstOccurrenceDate = "2024-01-15",
                    finalOccurrenceDate = "2024-05-31",
                    daysOfMonth = listOf(15),
                    daysOfWeek = null,
                    weeksOfMonth = null,
                    monthsOfYear = listOf(1, 3, 5),
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
    fun `monthly recurrence gives day of month precedence over week selectors and supports negative week selector`() {
        assertEquals(
            listOf("2024-01-05", "2024-01-29").map(LocalDate::parse),
            calculator.occurrences(
                pattern(
                    recurrenceType = RecurrenceType.MONTHLY,
                    firstOccurrenceDate = "2024-01-01",
                    finalOccurrenceDate = "2024-01-31",
                    daysOfWeek = listOf(DayOfWeek.MONDAY),
                    weeksOfMonth = listOf(-1),
                    daysOfMonth = listOf(5),
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
    fun `yearly recurrence supports same date and month-day restrictions with skip count`() {
        assertEquals(
            listOf("2024-06-15", "2026-06-15", "2028-06-15").map(LocalDate::parse),
            calculator.occurrences(
                pattern(
                    recurrenceType = RecurrenceType.YEARLY,
                    firstOccurrenceDate = "2024-06-15",
                    finalOccurrenceDate = "2028-12-31",
                    skipCount = 1,
                    daysOfWeek = null,
                    weeksOfMonth = null,
                    daysOfMonth = listOf(15),
                    monthsOfYear = listOf(6),
                ),
            ),
        )
        assertEquals(
            listOf("2024-03-10", "2025-03-10", "2026-03-10").map(LocalDate::parse),
            calculator.occurrences(
                pattern(
                    recurrenceType = RecurrenceType.YEARLY,
                    firstOccurrenceDate = "2024-03-10",
                    finalOccurrenceDate = "2026-12-31",
                    daysOfWeek = null,
                    weeksOfMonth = null,
                    daysOfMonth = null,
                    monthsOfYear = null,
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

    @Test
    fun `explicit final date overrides max recurrence count input`() {
        assertEquals(
            LocalDate.parse("2024-02-15"),
            calculator.normalizeFinalOccurrenceDate(
                pattern(
                    recurrenceType = RecurrenceType.MONTHLY,
                    firstOccurrenceDate = "2024-01-15",
                    finalOccurrenceDate = "2024-02-15",
                    daysOfMonth = listOf(15),
                    daysOfWeek = null,
                    weeksOfMonth = null,
                    monthsOfYear = null,
                ),
                maxRecurrenceCount = 12,
            ),
        )
    }

    @Test
    fun `invalid skip count is rejected and end before first yields empty`() {
        assertFailsWith<de.chennemann.plannr.server.common.error.ValidationException> {
            calculator.occurrences(pattern(recurrenceType = RecurrenceType.DAILY, skipCount = -1))
        }
        assertEquals(
            emptyList(),
            calculator.occurrences(pattern(recurrenceType = RecurrenceType.NONE, firstOccurrenceDate = "2024-01-10", finalOccurrenceDate = "2024-01-09")),
        )
    }

    private fun pattern(
        recurrenceType: RecurrenceType,
        firstOccurrenceDate: String = "2024-01-01",
        finalOccurrenceDate: String? = "2024-01-05",
        skipCount: Int = 0,
        daysOfWeek: List<DayOfWeek>? = null,
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
