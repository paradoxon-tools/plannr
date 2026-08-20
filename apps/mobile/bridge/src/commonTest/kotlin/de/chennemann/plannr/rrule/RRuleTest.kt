//package de.chennemann.plannr.rrule
//
//import de.chennemann.plannr.datetime.now
//import kotlinx.datetime.DayOfWeek
//import kotlinx.datetime.LocalDate
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertFailsWith
//import kotlin.test.assertTrue
//import kotlin.test.fail
//
//class RRuleTest {
//
//    @Test
//    fun test() {
//
//        val rrule = RRule(Frequency.WEEKLY)
//            .interval(2)
//            .count(10)
//            .byDay(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
//
//        println("RRule String: ${rrule}")
//
//        val start = LocalDate.now()
//        val occurrences = rrule.generateOccurrences(start).take(10).toList()
//        occurrences.forEach { println(it) }
//    }
//
//
//    @Test
//    fun testDailyRecurrence() {
//        val rule = RRule("FREQ=DAILY;COUNT=5")
//        val startDate = LocalDate(2023, 1, 1)
//        val occurrences = rule.generateOccurrences(startDate)
//        val expected = listOf(
//            LocalDate(2023, 1, 1),
//            LocalDate(2023, 1, 2),
//            LocalDate(2023, 1, 3),
//            LocalDate(2023, 1, 4),
//            LocalDate(2023, 1, 5)
//        )
//        assertEquals(expected, occurrences.toList())
//    }
//
//    @Test
//    fun testWeeklyRecurrenceWithDayOfWeek() {
//        val rule = RRule("FREQ=WEEKLY;BYDAY=MO,WE,FR;COUNT=6")
//        val startDate = LocalDate(2023, 1, 1) // Sunday
//        val occurrences = rule.generateOccurrences(startDate)
//        val expected = listOf(
//            LocalDate(2023, 1, 2), // Monday
//            LocalDate(2023, 1, 4), // Wednesday
//            LocalDate(2023, 1, 6), // Friday
//            LocalDate(2023, 1, 9), // Monday
//            LocalDate(2023, 1, 11), // Wednesday
//            LocalDate(2023, 1, 13) // Friday
//        )
//        assertEquals(expected, occurrences.toList())
//    }
//
//    @Test
//    fun testMonthlyRecurrenceWithDayOfMonth() {
//        val rule = RRule("FREQ=MONTHLY;BYMONTHDAY=15;COUNT=3")
//        val startDate = LocalDate(2023, 1, 1)
//        val occurrences = rule.generateOccurrences(startDate)
//        val expected = listOf(
//            LocalDate(2023, 1, 15),
//            LocalDate(2023, 2, 15),
//            LocalDate(2023, 3, 15)
//        )
//        assertEquals(expected, occurrences.toList())
//    }
//
//    @Test
//    fun testYearlyRecurrenceWithSpecificMonthAndDay() {
//        val rule = RRule("FREQ=YEARLY;BYMONTH=7;BYMONTHDAY=4;COUNT=3")
//        val startDate = LocalDate(2023, 1, 1)
//        val occurrences = rule.generateOccurrences(startDate)
//        val expected = listOf(
//            LocalDate(2023, 7, 4),
//            LocalDate(2024, 7, 4),
//            LocalDate(2025, 7, 4)
//        )
//        assertEquals(expected, occurrences.toList())
//    }
//
//    @Test
//    fun testRecurrenceWithUntilDate() {
//        val rule = RRule("FREQ=DAILY;UNTIL=20230105T000000Z")
//        val startDate = LocalDate(2023, 1, 1)
//        val occurrences = rule.generateOccurrences(startDate)
//        val expected = listOf(
//            LocalDate(2023, 1, 1),
//            LocalDate(2023, 1, 2),
//            LocalDate(2023, 1, 3),
//            LocalDate(2023, 1, 4),
//            LocalDate(2023, 1, 5)
//        )
//        assertEquals(expected, occurrences.toList())
//    }
//
//    @Test
//    fun testHourlyRecurrence() {
//        val rule = RRule("FREQ=HOURLY;COUNT=3")
//        val startDate = LocalDate(2023, 1, 1)
//        val occurrences = rule.generateOccurrences(startDate)
//        val expected = listOf(
//            LocalDate(2023, 1, 1),
//            LocalDate(2023, 1, 1),
//            LocalDate(2023, 1, 1)
//        )
//        assertEquals(expected, occurrences.toList())
//    }
//
//    @Test
//    fun testInvalidRRule() {
//        assertFailsWith<IllegalArgumentException> {
//            RRule("FREQ=INVALID;COUNT=5")
//        }
//    }
//
//    @Test
//    fun testNoOccurrences() {
//        val rule = RRule("FREQ=DAILY;UNTIL=20221231T000000Z")
//        val startDate = LocalDate(2023, 1, 1)
//        val occurrences = rule.generateOccurrences(startDate)
//        assertTrue(occurrences.count() == 0, "Expected no occurrences")
//    }
//
//    @Test
//    fun testRecurrenceWithBySetPos() {
//        val rule = RRule("FREQ=MONTHLY;BYDAY=MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY;BYSETPOS=-1;COUNT=3")
//        val startDate = LocalDate(2023, 1, 1)
//        val occurrences = rule.generateOccurrences(startDate)
//        val expected = listOf(
//            LocalDate(2023, 1, 31), // Last weekday of January
//            LocalDate(2023, 2, 28), // Last weekday of February
//            LocalDate(2023, 3, 31)  // Last weekday of March
//        )
//        assertEquals(expected, occurrences.toList())
//    }
//
//    @Test
//    fun testRecurrenceWithByWeekNo() {
//        val rule = RRule("FREQ=YEARLY;BYWEEKNO=20;COUNT=3")
//        val startDate = LocalDate(2023, 1, 1)
//        val occurrences = rule.generateOccurrences(startDate)
//        val expected = listOf(
//            LocalDate(2023, 5, 15), // First day of week 20 in 2023
//            LocalDate(2024, 5, 13), // First day of week 20 in 2024
//            LocalDate(2025, 5, 12)  // First day of week 20 in 2025
//        )
//        assertEquals(expected, occurrences.toList())
//    }
//
//}