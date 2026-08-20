package de.chennemann.plannr.data

import de.chennemann.plannr.data.TransactionTemplate.RecurrencePattern
import de.chennemann.plannr.datetime.plusDays
import de.chennemann.plannr.datetime.plusMonths
import de.chennemann.plannr.datetime.plusWeeks
import de.chennemann.plannr.datetime.plusYears
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Clock

class TransactionTemplateTest {

    @Test
    fun `-IF- recurrenceType != NONE -THEN- isRecurring == true`() {
        val template = createTemplate(recurrenceType = RecurrenceType.DAILY)
        assertEquals(true, template.isRecurring)
    }

    @Test
    fun `-IF- recurrenceType NONE -THEN- only initial date is returned`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val template = createTemplate(
            recurrenceType = RecurrenceType.NONE,
            startDate = start
        )

        val dates = template.getTransactionDates().toList()
        assertEquals(listOf(start), dates)
    }

    @Test
    fun `-IF- recurrenceType DAILY -THEN- dates increment by one day`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val template = createTemplate(
            recurrenceType = RecurrenceType.DAILY,
            startDate = start
        )

        val dates = template.getTransactionDates().take(5).toList()
        assertEquals(
            listOf(
                start,
                start.plusDays(1),
                start.plusDays(2),
                start.plusDays(3),
                start.plusDays(4),
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType DAILY -AND- skipCount == 1 -THEN- dates increment by two days`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val template = createTemplate(
            recurrenceType = RecurrenceType.DAILY,
            skipCount = 1,
            startDate = start
        )

        val dates = template.getTransactionDates().take(5).toList()
        assertEquals(
            listOf(
                start,
                start.plusDays(2),
                start.plusDays(4),
                start.plusDays(6),
                start.plusDays(8),
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType DAILY -AND- skipCount == 2 -THEN- dates increment by three days`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val template = createTemplate(
            recurrenceType = RecurrenceType.DAILY,
            skipCount = 2,
            startDate = start
        )

        val dates = template.getTransactionDates().take(5).toList()
        assertEquals(
            listOf(
                start,
                start.plusDays(3),
                start.plusDays(6),
                start.plusDays(9),
                start.plusDays(12),
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType WEEKLY -THEN- dates increment by one week`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val template = createTemplate(
            recurrenceType = RecurrenceType.WEEKLY,
            startDate = start
        )

        val dates = template.getTransactionDates().take(5).toList()
        assertEquals(
            listOf(
                start,
                start.plusWeeks(1),
                start.plusWeeks(2),
                start.plusWeeks(3),
                start.plusWeeks(4),
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType WEEKLY -AND- skipCount == 1 -THEN- dates increment by two weeks`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val template = createTemplate(
            recurrenceType = RecurrenceType.WEEKLY,
            skipCount = 1,
            startDate = start
        )

        val dates = template.getTransactionDates().take(5).toList()
        assertEquals(
            listOf(
                start,
                start.plusWeeks(2),
                start.plusWeeks(4),
                start.plusWeeks(6),
                start.plusWeeks(8),
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType WEEKLY -AND- skipCount == 2 -THEN- dates increment by three weeks`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val template = createTemplate(
            recurrenceType = RecurrenceType.WEEKLY,
            skipCount = 2,
            startDate = start
        )

        val dates = template.getTransactionDates().take(5).toList()
        assertEquals(
            listOf(
                start,
                start.plusWeeks(3),
                start.plusWeeks(6),
                start.plusWeeks(9),
                start.plusWeeks(12),
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType WEEKLY -AND- daysOfWeek contains single day -THEN- dates are on that day of week`() {
        // 2025-01-01 is a Wednesday
        val start = LocalDate(2025, Month.JANUARY, 1)
        // We want to test with Monday
        val dayOfWeek = DayOfWeek.MONDAY

        val template = createTemplate(
            recurrenceType = RecurrenceType.WEEKLY,
            startDate = start,
            daysOfWeek = listOf(dayOfWeek)
        )

        val dates = template.getTransactionDates().take(5).toList()

        // First date should be the first Monday on or after the start date (2025-01-06)
        val firstMonday = LocalDate(2025, Month.JANUARY, 6)

        assertEquals(
            listOf(
                firstMonday,
                firstMonday.plusWeeks(1), // 2025-01-13
                firstMonday.plusWeeks(2), // 2025-01-20
                firstMonday.plusWeeks(3), // 2025-01-27
                firstMonday.plusWeeks(4), // 2025-02-03
            ),
            dates
        )

        // Verify all dates are Mondays
        dates.forEach { date ->
            assertEquals(dayOfWeek, date.dayOfWeek)
        }
    }

    @Test
    fun `-IF- recurrenceType WEEKLY -AND- daysOfWeek contains multiple days -THEN- dates are on those days of week`() {
        // 2025-01-01 is a Wednesday
        val start = LocalDate(2025, Month.JANUARY, 1)
        // We want to test with Monday and Friday
        val daysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)

        val template = createTemplate(
            recurrenceType = RecurrenceType.WEEKLY,
            startDate = start,
            daysOfWeek = daysOfWeek
        )

        val dates = template.getTransactionDates().take(5).toList()

        // First date should be the first Friday on or after the start date (2025-01-03)
        val firstFriday = LocalDate(2025, Month.JANUARY, 3)
        // Second date should be the first Monday after the first Friday (2025-01-06)
        val firstMonday = LocalDate(2025, Month.JANUARY, 6)

        assertEquals(
            listOf(
                firstFriday,  // 2025-01-03 (Friday)
                firstMonday,  // 2025-01-06 (Monday)
                firstFriday.plusWeeks(1),  // 2025-01-10 (Friday)
                firstMonday.plusWeeks(1),  // 2025-01-13 (Monday)
                firstFriday.plusWeeks(2),  // 2025-01-17 (Friday)
            ),
            dates
        )

        // Verify all dates are either Monday or Friday
        dates.forEach { date ->
            assertTrue(daysOfWeek.contains(date.dayOfWeek))
        }
    }

    @Test
    fun `-IF- recurrenceType WEEKLY -AND- start date matches daysOfWeek -THEN- start date is used`() {
        // 2025-01-01 is a Wednesday
        val start = LocalDate(2025, Month.JANUARY, 1)
        val dayOfWeek = DayOfWeek.WEDNESDAY

        val template = createTemplate(
            recurrenceType = RecurrenceType.WEEKLY,
            startDate = start,
            daysOfWeek = listOf(dayOfWeek)
        )

        val dates = template.getTransactionDates().take(5).toList()

        assertEquals(
            listOf(
                start,  // 2025-01-01 (Wednesday)
                start.plusWeeks(1),  // 2025-01-08 (Wednesday)
                start.plusWeeks(2),  // 2025-01-15 (Wednesday)
                start.plusWeeks(3),  // 2025-01-22 (Wednesday)
                start.plusWeeks(4),  // 2025-01-29 (Wednesday)
            ),
            dates
        )

        // Verify all dates are Wednesdays
        dates.forEach { date ->
            assertEquals(dayOfWeek, date.dayOfWeek)
        }
    }

    @Test
    fun `-IF- recurrenceType WEEKLY -AND- daysOfWeek with skipCount -THEN- dates are on those days with correct interval`() {
        // 2025-01-01 is a Wednesday
        val start = LocalDate(2025, Month.JANUARY, 1)
        // We want to test with Monday and Friday
        val daysOfWeek = listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)

        val template = createTemplate(
            recurrenceType = RecurrenceType.WEEKLY,
            skipCount = 1, // Biweekly
            startDate = start,
            daysOfWeek = daysOfWeek
        )

        val dates = template.getTransactionDates().take(5).toList()

        // First date should be the first Friday on or after the start date (2025-01-03)
        val firstFriday = LocalDate(2025, Month.JANUARY, 3)
        // Second date should be the first Monday after the first Friday (2025-01-06)
        val firstMonday = LocalDate(2025, Month.JANUARY, 6)
        // Third date should be the Friday 2 weeks after the first Friday (2025-01-17)
        val thirdFriday = firstFriday.plusWeeks(2)
        // Fourth date should be the Monday 2 weeks after the first Monday (2025-01-20)
        val thirdMonday = firstMonday.plusWeeks(2)
        // Fifth date should be the Friday 4 weeks after the first Friday (2025-01-31)
        val fifthFriday = firstFriday.plusWeeks(4)

        assertEquals(
            listOf(
                firstFriday,  // 2025-01-03 (Friday)
                firstMonday,  // 2025-01-06 (Monday)
                thirdFriday,  // 2025-01-17 (Friday)
                thirdMonday,  // 2025-01-20 (Monday)
                fifthFriday,  // 2025-01-31 (Friday)
            ),
            dates
        )

        // Verify all dates are either Monday or Friday
        dates.forEach { date ->
            assertTrue(daysOfWeek.contains(date.dayOfWeek))
        }
    }

    @Test
    fun `-IF- recurrenceType MONTHLY -THEN- dates increment by one month`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val template = createTemplate(
            recurrenceType = RecurrenceType.MONTHLY,
            startDate = start
        )

        val dates = template.getTransactionDates().take(5).toList()
        assertEquals(
            listOf(
                start,
                start.plusMonths(1),  // 2025-02-01
                start.plusMonths(2),  // 2025-03-01
                start.plusMonths(3),  // 2025-04-01
                start.plusMonths(4),  // 2025-05-01
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType MONTHLY -AND- skipCount == 1 -THEN- dates increment by two months`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val template = createTemplate(
            recurrenceType = RecurrenceType.MONTHLY,
            skipCount = 1,
            startDate = start
        )

        val dates = template.getTransactionDates().take(5).toList()
        assertEquals(
            listOf(
                start,
                start.plusMonths(2),  // 2025-03-01
                start.plusMonths(4),  // 2025-05-01
                start.plusMonths(6),  // 2025-07-01
                start.plusMonths(8),  // 2025-09-01
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType MONTHLY -AND- skipCount == 2 -THEN- dates increment by three months`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val template = createTemplate(
            recurrenceType = RecurrenceType.MONTHLY,
            skipCount = 2,
            startDate = start
        )

        val dates = template.getTransactionDates().take(5).toList()
        assertEquals(
            listOf(
                start,
                start.plusMonths(3),  // 2025-04-01
                start.plusMonths(6),  // 2025-07-01
                start.plusMonths(9),  // 2025-10-01
                start.plusMonths(12),  // 2026-01-01
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType MONTHLY -AND- daysOfMonth contains single day -THEN- dates are on that day of month`() {
        // 2025-01-15 is the 15th day of January
        val start = LocalDate(2025, Month.JANUARY, 15)
        // We want to test with the 10th day of the month
        val dayOfMonth = 10

        val template = createTemplate(
            recurrenceType = RecurrenceType.MONTHLY,
            startDate = start,
            daysOfMonth = listOf(dayOfMonth)
        )

        val dates = template.getTransactionDates().take(5).toList()

        // First date should be the 10th day of February (since January 10th is before our start date)
        val firstDate = LocalDate(2025, Month.FEBRUARY, 10)

        assertEquals(
            listOf(
                firstDate,  // 2025-02-10
                firstDate.plusMonths(1),  // 2025-03-10
                firstDate.plusMonths(2),  // 2025-04-10
                firstDate.plusMonths(3),  // 2025-05-10
                firstDate.plusMonths(4),  // 2025-06-10
            ),
            dates
        )

        // Verify all dates are on the 10th day of the month
        dates.forEach { date ->
            assertEquals(dayOfMonth, date.day)
        }
    }

    @Test
    fun `-IF- recurrenceType MONTHLY -AND- daysOfMonth contains negative day -THEN- dates are on that day from end of month`() {
        // 2025-01-15 is the 15th day of January
        val start = LocalDate(2025, Month.JANUARY, 15)
        // We want to test with the last day of the month (-1)
        val dayOfMonth = -1

        val template = createTemplate(
            recurrenceType = RecurrenceType.MONTHLY,
            startDate = start,
            daysOfMonth = listOf(dayOfMonth)
        )

        val dates = template.getTransactionDates().take(5).toList()

        // First date should be the last day of January (since it's after our start date)
        val firstDate = LocalDate(2025, Month.JANUARY, 31)
        // February has 28 days in 2025
        val secondDate = LocalDate(2025, Month.FEBRUARY, 28)
        // March has 31 days
        val thirdDate = LocalDate(2025, Month.MARCH, 31)
        // April has 30 days
        val fourthDate = LocalDate(2025, Month.APRIL, 30)
        // May has 31 days
        val fifthDate = LocalDate(2025, Month.MAY, 31)

        assertEquals(
            listOf(
                firstDate,  // 2025-01-31
                secondDate,  // 2025-02-28
                thirdDate,  // 2025-03-31
                fourthDate,  // 2025-04-30
                fifthDate,  // 2025-05-31
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType MONTHLY -AND- monthsOfYear specified -THEN- dates are only in those months`() {
        // 2025-01-15 is in January
        val start = LocalDate(2025, Month.JANUARY, 15)
        // We want to test with April, July, and October
        val monthsOfYear = listOf(Month.APRIL, Month.JULY, Month.OCTOBER)

        val template = createTemplate(
            recurrenceType = RecurrenceType.MONTHLY,
            startDate = start,
            monthsOfYear = monthsOfYear
        )

        val dates = template.getTransactionDates().take(5).toList()

        // First date should be April 15, 2025 (first month in our list after January)
        val firstDate = LocalDate(2025, Month.APRIL, 15)
        // Second date should be July 15, 2025
        val secondDate = LocalDate(2025, Month.JULY, 15)
        // Third date should be October 15, 2025
        val thirdDate = LocalDate(2025, Month.OCTOBER, 15)
        // Fourth date should be April 15, 2026
        val fourthDate = LocalDate(2026, Month.APRIL, 15)
        // Fifth date should be July 15, 2026
        val fifthDate = LocalDate(2026, Month.JULY, 15)

        assertEquals(
            listOf(
                firstDate,  // 2025-04-15
                secondDate,  // 2025-07-15
                thirdDate,  // 2025-10-15
                fourthDate,  // 2026-04-15
                fifthDate,  // 2026-07-15
            ),
            dates
        )

        // Verify all dates are in one of the specified months
        dates.forEach { date ->
            assertTrue(monthsOfYear.contains(date.month))
        }
    }

    @Test
    fun `-IF- recurrenceType MONTHLY -AND- daysOfMonth and monthsOfYear specified -THEN- dates match both criteria`() {
        // 2025-01-15 is in January
        val start = LocalDate(2025, Month.JANUARY, 15)
        // We want to test with the 11th day of the month
        val dayOfMonth = 11
        // We want to test with January, April, July, and October (quarterly)
        val monthsOfYear = listOf(Month.JANUARY, Month.APRIL, Month.JULY, Month.OCTOBER)

        val template = createTemplate(
            recurrenceType = RecurrenceType.MONTHLY,
            startDate = start,
            daysOfMonth = listOf(dayOfMonth),
            monthsOfYear = monthsOfYear
        )

        val dates = template.getTransactionDates().take(5).toList()

        // First date should be April 11, 2025 (since January 11 is before our start date)
        val firstDate = LocalDate(2025, Month.APRIL, 11)
        // Second date should be July 11, 2025
        val secondDate = LocalDate(2025, Month.JULY, 11)
        // Third date should be October 11, 2025
        val thirdDate = LocalDate(2025, Month.OCTOBER, 11)
        // Fourth date should be January 11, 2026
        val fourthDate = LocalDate(2026, Month.JANUARY, 11)
        // Fifth date should be April 11, 2026
        val fifthDate = LocalDate(2026, Month.APRIL, 11)

        assertEquals(
            listOf(
                firstDate,  // 2025-04-11
                secondDate,  // 2025-07-11
                thirdDate,  // 2025-10-11
                fourthDate,  // 2026-01-11
                fifthDate,  // 2026-04-11
            ),
            dates
        )

        // Verify all dates are on the 11th day of the month
        dates.forEach { date ->
            assertEquals(dayOfMonth, date.day)
        }

        // Verify all dates are in one of the specified months
        dates.forEach { date ->
            assertTrue(monthsOfYear.contains(date.month))
        }
    }

    @Test
    fun `-IF- recurrenceType MONTHLY -AND- quarterly event with skipCount -THEN- dates are quarterly`() {
        // Example from the issue description:
        // A quarterly event on the 11th day of the first month in each quarter (January, April, July, October)
        // with separation_count (skipCount) of 2

        val start = LocalDate(2025, Month.JANUARY, 1)
        val dayOfMonth = 11

        val template = createTemplate(
            recurrenceType = RecurrenceType.MONTHLY,
            skipCount = 2,
            daysOfMonth = listOf(dayOfMonth),
            startDate = start
        )

        val dates = template.getTransactionDates().take(5).toList()

        // First date should be January 11, 2025
        val firstDate = LocalDate(2025, Month.JANUARY, 11)
        // Second date should be April 11, 2025 (January + 3 months)
        val secondDate = LocalDate(2025, Month.APRIL, 11)
        // Third date should be July 11, 2025 (April + 3 months)
        val thirdDate = LocalDate(2025, Month.JULY, 11)
        // Fourth date should be October 11, 2025 (July + 3 months)
        val fourthDate = LocalDate(2025, Month.OCTOBER, 11)
        // Fifth date should be January 11, 2026 (October + 3 months)
        val fifthDate = LocalDate(2026, Month.JANUARY, 11)

        assertEquals(
            listOf(
                firstDate,  // 2025-01-11
                secondDate,  // 2025-04-11
                thirdDate,  // 2025-07-11
                fourthDate,  // 2025-10-11
                fifthDate,  // 2026-01-11
            ),
            dates
        )

        // Verify all dates are on the 11th day of the month
        dates.forEach { date ->
            assertEquals(dayOfMonth, date.day)
        }
    }

    @Test
    fun `-IF- recurrenceType MONTHLY -AND- daysOfMonth contains multiple days -THEN- dates include all specified days`() {
        // 2025-01-15 is the 15th day of January
        val start = LocalDate(2025, Month.JANUARY, 15)
        // We want to test with the 5th and 20th days of the month
        val daysOfMonth = listOf(5, 20)

        val template = createTemplate(
            recurrenceType = RecurrenceType.MONTHLY,
            startDate = start,
            daysOfMonth = daysOfMonth
        )

        val dates = template.getTransactionDates().take(6).toList()

        // First date should be January 20, 2025 (since it's after our start date)
        val firstDate = LocalDate(2025, Month.JANUARY, 20)
        // Second date should be February 5, 2025
        val secondDate = LocalDate(2025, Month.FEBRUARY, 5)
        // Third date should be February 20, 2025
        val thirdDate = LocalDate(2025, Month.FEBRUARY, 20)
        // Fourth date should be March 5, 2025
        val fourthDate = LocalDate(2025, Month.MARCH, 5)
        // Fifth date should be March 20, 2025
        val fifthDate = LocalDate(2025, Month.MARCH, 20)
        // Sixth date should be April 5, 2025
        val sixthDate = LocalDate(2025, Month.APRIL, 5)

        assertEquals(
            listOf(
                firstDate,   // 2025-01-20
                secondDate,  // 2025-02-05
                thirdDate,   // 2025-02-20
                fourthDate,  // 2025-03-05
                fifthDate,   // 2025-03-20
                sixthDate,   // 2025-04-05
            ),
            dates
        )

        // Verify all dates are on either the 5th or 20th day of the month
        dates.forEach { date ->
            assertTrue(daysOfMonth.contains(date.day))
        }
    }

    @Test
    fun `-IF- recurrenceType YEARLY -THEN- dates increment by one year`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val template = createTemplate(
            recurrenceType = RecurrenceType.YEARLY,
            startDate = start
        )

        val dates = template.getTransactionDates().take(5).toList()
        assertEquals(
            listOf(
                start,
                start.plusYears(1),  // 2026-01-01
                start.plusYears(2),  // 2027-01-01
                start.plusYears(3),  // 2028-01-01
                start.plusYears(4),  // 2029-01-01
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType YEARLY -AND- skipCount == 1 -THEN- dates increment by two years`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val template = createTemplate(
            recurrenceType = RecurrenceType.YEARLY,
            skipCount = 1,
            startDate = start
        )

        val dates = template.getTransactionDates().take(5).toList()
        assertEquals(
            listOf(
                start,
                start.plusYears(2),  // 2027-01-01
                start.plusYears(4),  // 2029-01-01
                start.plusYears(6),  // 2031-01-01
                start.plusYears(8),  // 2033-01-01
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType YEARLY -AND- skipCount == 2 -THEN- dates increment by three years`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val template = createTemplate(
            recurrenceType = RecurrenceType.YEARLY,
            skipCount = 2,
            startDate = start
        )

        val dates = template.getTransactionDates().take(5).toList()
        assertEquals(
            listOf(
                start,
                start.plusYears(3),  // 2028-01-01
                start.plusYears(6),  // 2031-01-01
                start.plusYears(9),  // 2034-01-01
                start.plusYears(12),  // 2037-01-01
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType YEARLY -AND- daysOfMonth contains single day -THEN- dates are on that day of month`() {
        // 2025-01-15 is the 15th day of January
        val start = LocalDate(2025, Month.JANUARY, 15)
        // We want to test with the 10th day of the month
        val dayOfMonth = 11

        val template = createTemplate(
            recurrenceType = RecurrenceType.YEARLY,
            startDate = start,
            daysOfMonth = listOf(dayOfMonth)
        )

        val dates = template.getTransactionDates().take(5).toList()

        // First date should be the 10th day of February 2025 (since January 10th, 2025 is before our start date)
        val firstDate = LocalDate(2025, Month.FEBRUARY, 11)

        assertEquals(
            listOf(
                firstDate,  // 2025-02-10
                firstDate.plusYears(1),  // 2026-02-10
                firstDate.plusYears(2),  // 2027-02-10
                firstDate.plusYears(3),  // 2028-02-10
                firstDate.plusYears(4),  // 2029-02-10
            ),
            dates
        )

        // Verify all dates are on the 10th day of the month
        dates.forEach { date ->
            assertEquals(dayOfMonth, date.day)
        }
    }

    @Test
    fun `-IF- recurrenceType YEARLY -AND- monthsOfYear specified -THEN- dates are only in those months`() {
        // 2025-01-15 is in January
        val start = LocalDate(2025, Month.JANUARY, 15)
        // We want to test with April
        val monthOfYear = Month.APRIL

        val template = createTemplate(
            recurrenceType = RecurrenceType.YEARLY,
            startDate = start,
            monthsOfYear = listOf(monthOfYear)
        )

        val dates = template.getTransactionDates().take(5).toList()

        // First date should be April 15, 2025 (first month in our list after January 2025)
        val firstDate = LocalDate(2025, Month.APRIL, 15)

        assertEquals(
            listOf(
                firstDate,  // 2025-04-15
                firstDate.plusYears(1),  // 2026-04-15
                firstDate.plusYears(2),  // 2027-04-15
                firstDate.plusYears(3),  // 2028-04-15
                firstDate.plusYears(4),  // 2029-04-15
            ),
            dates
        )

        // Verify all dates are in April
        dates.forEach { date ->
            assertEquals(monthOfYear, date.month)
        }
    }

    @Test
    fun `-IF- recurrenceType YEARLY -AND- daysOfMonth and monthsOfYear specified -THEN- dates match both criteria`() {
        // 2025-01-15 is in January
        val start = LocalDate(2025, Month.JANUARY, 15)
        // We want to test with the 10th day of the month
        val dayOfMonth = 10
        // We want to test with April
        val monthOfYear = Month.APRIL

        val template = createTemplate(
            recurrenceType = RecurrenceType.YEARLY,
            startDate = start,
            daysOfMonth = listOf(dayOfMonth),
            monthsOfYear = listOf(monthOfYear)
        )

        val dates = template.getTransactionDates().take(5).toList()

        // First date should be April 10, 2026 (first month in our list after January 2025, with the specified day)
        val firstDate = LocalDate(2026, Month.APRIL, 10)

        assertEquals(
            listOf(
                firstDate,  // 2026-04-10
                firstDate.plusYears(1),  // 2027-04-10
                firstDate.plusYears(2),  // 2028-04-10
                firstDate.plusYears(3),  // 2029-04-10
                firstDate.plusYears(4),  // 2030-04-10
            ),
            dates
        )

        // Verify all dates are on the 10th day of the month
        dates.forEach { date ->
            assertEquals(dayOfMonth, date.day)
        }

        // Verify all dates are in April
        dates.forEach { date ->
            assertEquals(monthOfYear, date.month)
        }
    }

    private fun createTemplate(
        transactionTemplateId: Long = 43,
        title: String = "Some Transaction Template",
        description: String = "Some description",
        contractId: Long? = null,
        recurrenceType: RecurrenceType = RecurrenceType.NONE,
        maxRecurrenceCount: Int? = null,
        skipCount: Int = 0,
        daysOfWeek: List<DayOfWeek> = emptyList(),
        weeksOfMonth: List<Int> = emptyList(),
        daysOfMonth: List<Int> = emptyList(),
        monthsOfYear: List<Month> = emptyList(),
        startDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
        endDate: LocalDate? = null,
        amount: Long = 0,
        currency: String = "EUR",
        nextOccurrence: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
        sourcePocketId: Long? = null,
        destinationPocketId: Long? = null,
        partnerId: Long? = null,
    ) = TransactionTemplate(
        id = transactionTemplateId,
        title = title,
        description = description,
        contractId = contractId,
        recurrencePattern = RecurrencePattern(
            referenceDate = startDate,
            recurrenceType = recurrenceType,
            finalOccurrenceDate = endDate,
            maxRecurrenceCount = maxRecurrenceCount,
            skipCount = skipCount,
            daysOfWeek = daysOfWeek,
            weeksOfMonth = weeksOfMonth,
            daysOfMonth = daysOfMonth,
            monthsOfYear = monthsOfYear
        ),
        amount = Amount(
            amount = amount,
            currency = currency,
        ),
        sourcePocketId = sourcePocketId,
        destinationPocketId = destinationPocketId,
        partnerId = partnerId
    )

    @Test
    fun `-IF- recurrenceType NONE -AND- endDate specified -THEN- only startDate is returned if not after endDate`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val end = LocalDate(2025, Month.JANUARY, 10)

        // Test with startDate before endDate
        val template1 = createTemplate(
            recurrenceType = RecurrenceType.NONE,
            startDate = start,
            endDate = end
        )

        val dates1 = template1.getTransactionDates().toList()
        assertEquals(listOf(start), dates1)

        // Test with startDate equal to endDate
        val template2 = createTemplate(
            recurrenceType = RecurrenceType.NONE,
            startDate = end,
            endDate = end
        )

        val dates2 = template2.getTransactionDates().toList()
        assertEquals(listOf(end), dates2)

        // Test with startDate after endDate
        assertFailsWith<IllegalArgumentException> {
            createTemplate(
                recurrenceType = RecurrenceType.NONE,
                startDate = end.plusDays(1),
                endDate = end
            )
        }
    }

    @Test
    fun `-IF- recurrenceType DAILY -AND- endDate specified -THEN- dates stop at endDate`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val end = LocalDate(2025, Month.JANUARY, 5)

        val template = createTemplate(
            recurrenceType = RecurrenceType.DAILY,
            startDate = start,
            endDate = end
        )

        val dates = template.getTransactionDates().toList()
        assertEquals(
            listOf(
                start,                // 2025-01-01
                start.plusDays(1),    // 2025-01-02
                start.plusDays(2),    // 2025-01-03
                start.plusDays(3),    // 2025-01-04
                start.plusDays(4),    // 2025-01-05
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType WEEKLY -AND- endDate specified -THEN- dates stop at endDate`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val end = LocalDate(2025, Month.FEBRUARY, 5)

        val template = createTemplate(
            recurrenceType = RecurrenceType.WEEKLY,
            startDate = start,
            endDate = end
        )

        val dates = template.getTransactionDates().toList()
        assertEquals(
            listOf(
                start,                 // 2025-01-01
                start.plusWeeks(1),    // 2025-01-08
                start.plusWeeks(2),    // 2025-01-15
                start.plusWeeks(3),    // 2025-01-22
                start.plusWeeks(4),    // 2025-01-29
                start.plusWeeks(5),    // 2025-02-05
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType MONTHLY -AND- endDate specified -THEN- dates stop at endDate`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val end = LocalDate(2025, Month.MAY, 1)

        val template = createTemplate(
            recurrenceType = RecurrenceType.MONTHLY,
            startDate = start,
            endDate = end
        )

        val dates = template.getTransactionDates().toList()
        assertEquals(
            listOf(
                start,                  // 2025-01-01
                start.plusMonths(1),    // 2025-02-01
                start.plusMonths(2),    // 2025-03-01
                start.plusMonths(3),    // 2025-04-01
                start.plusMonths(4),    // 2025-05-01
            ),
            dates
        )
    }

    @Test
    fun `-IF- recurrenceType YEARLY -AND- endDate specified -THEN- dates stop at endDate`() {
        val start = LocalDate(2025, Month.JANUARY, 1)
        val end = LocalDate(2029, Month.JANUARY, 1)

        val template = createTemplate(
            recurrenceType = RecurrenceType.YEARLY,
            startDate = start,
            endDate = end
        )

        val dates = template.getTransactionDates().toList()
        assertEquals(
            listOf(
                start,                 // 2025-01-01
                start.plusYears(1),    // 2026-01-01
                start.plusYears(2),    // 2027-01-01
                start.plusYears(3),    // 2028-01-01
                start.plusYears(4),    // 2029-01-01
            ),
            dates
        )
    }
}
