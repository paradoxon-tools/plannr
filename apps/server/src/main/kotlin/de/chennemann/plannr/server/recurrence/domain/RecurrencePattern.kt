package de.chennemann.plannr.server.recurrence.domain

import de.chennemann.plannr.server.common.domain.RecurrenceType
import java.time.DayOfWeek
import java.time.LocalDate

data class RecurrencePattern(
    val firstOccurrenceDate: LocalDate,
    val finalOccurrenceDate: LocalDate?,
    val recurrenceType: RecurrenceType,
    val skipCount: Int,
    val daysOfWeek: List<DayOfWeek>?,
    val weeksOfMonth: List<Int>?,
    val daysOfMonth: List<Int>?,
    val monthsOfYear: List<Int>?,
)
