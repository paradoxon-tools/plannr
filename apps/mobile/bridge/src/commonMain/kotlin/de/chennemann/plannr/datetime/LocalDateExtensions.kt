package de.chennemann.plannr.datetime

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Instant

const val DAY_HOURS = 24L
val LocalTime.Companion.MIN get() = LocalTime(0, 0)
val LocalTime.Companion.MAX get() = LocalTime(23, 59, 59, 999999999)

fun LocalDateTime.Companion.now(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime =
    Clock.System.now().toLocalDateTime(timeZone)

fun LocalDate.Companion.now(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    LocalDateTime.now(timeZone).date

fun LocalDate.atStartOfDay(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant =
    this.atStartOfDayIn(timeZone)

fun LocalDate.atEndOfDay(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    return this
        .plus(1, DateTimeUnit.DAY)
        .atStartOfDayIn(timeZone).minus(1.nanoseconds)
}
fun LocalDate.atMidday(timeZone: TimeZone = TimeZone.currentSystemDefault()) = atTime(12, 0, 0).toInstant(timeZone)


fun LocalDateTime.evaluateDurationUntil(end: LocalDateTime, timeZone: TimeZone = TimeZone.currentSystemDefault()): Duration {
    if (end < this) throw IllegalArgumentException("end must be greater than start")
    return end.toInstant(timeZone) - this.toInstant(timeZone)
}

fun LocalDate.plusDays(days: Int): LocalDate = this.plusDays(days.toLong())
fun LocalDate.plusDays(days: Long): LocalDate = this.plus(days, DateTimeUnit.DAY)
fun LocalDate.plusWeeks(weeks: Int): LocalDate = this.plusWeeks(weeks.toLong())
fun LocalDate.plusWeeks(weeks: Long): LocalDate = this.plus(weeks, DateTimeUnit.WEEK)
fun LocalDate.plusMonths(months: Int): LocalDate = this.plusMonths(months.toLong())
fun LocalDate.plusMonths(months: Long): LocalDate = this.plus(months, DateTimeUnit.MONTH)
fun LocalDate.plusYears(years: Int): LocalDate = this.plusYears(years.toLong())
fun LocalDate.plusYears(years: Long): LocalDate = this.plus(years, DateTimeUnit.YEAR)

/**
 * Checks if this date is after the other date.
 * @return `true` if `this > other` or if `other == null`, false otherwise.
 */
fun LocalDate.isAfter(other: LocalDate): Boolean = this > other
fun LocalDate.isAfterOrEqual(other: LocalDate): Boolean = this >= other

/**
 * Checks if this date is before the other date.
 * @return `true` if `this < other` or if `other == null`, false otherwise.
 */
fun LocalDate.isBefore(other: LocalDate): Boolean = this < other
fun LocalDate.isBeforeOrEqual(other: LocalDate): Boolean = this <= other
