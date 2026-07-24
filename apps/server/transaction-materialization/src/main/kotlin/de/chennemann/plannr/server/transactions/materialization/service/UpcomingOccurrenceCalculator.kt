package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.common.domain.RecurrenceType
import de.chennemann.plannr.server.transactions.materialization.domain.RecurrenceCalculator
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import org.springframework.stereotype.Component

@Component
internal class UpcomingOccurrenceCalculator(
    private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculator(),
) {
    fun nextExpansionAfter(
        transactionTemplate: TransactionTemplate,
        afterExclusive: LocalDate,
    ): List<LocalDate> {
        val pattern = transactionTemplate.recurrencePattern
        val explicitEnd = pattern.finalOccurrenceDate?.let(LocalDate::parse)
        val firstOccurrenceDate = LocalDate.parse(pattern.firstOccurrenceDate)
        if (RecurrenceType.valueOf(pattern.recurrenceType) == RecurrenceType.NONE) {
            return if (firstOccurrenceDate.isAfter(afterExclusive)) listOf(firstOccurrenceDate) else emptyList()
        }
        if (explicitEnd != null && !explicitEnd.isAfter(afterExclusive)) {
            return emptyList()
        }

        var searchYears = INITIAL_SEARCH_YEARS
        repeat(MAX_SEARCH_ATTEMPTS) {
            val searchEnd = minOf(
                explicitEnd ?: LocalDate.MAX,
                runCatching { maxOf(afterExclusive, firstOccurrenceDate).plusYears(searchYears) }
                    .getOrDefault(LocalDate.MAX),
            )
            val futureOccurrences = recurrenceCalculator
                .occurrences(pattern = pattern, endInclusive = searchEnd)
                .filter { it.isAfter(afterExclusive) }

            if (futureOccurrences.isNotEmpty()) {
                val firstCycle = cycleOf(pattern.recurrenceType, futureOccurrences.first())
                return futureOccurrences.takeWhile { cycleOf(pattern.recurrenceType, it) == firstCycle }
            }
            if (explicitEnd != null && searchEnd == explicitEnd) {
                return emptyList()
            }
            searchYears *= 2
        }
        return emptyList()
    }

    private fun cycleOf(recurrenceType: String, date: LocalDate): Any =
        when (RecurrenceType.valueOf(recurrenceType)) {
            RecurrenceType.NONE,
            RecurrenceType.DAILY,
            -> date
            RecurrenceType.WEEKLY -> date.with(DayOfWeek.MONDAY)
            RecurrenceType.MONTHLY -> YearMonth.from(date)
            RecurrenceType.YEARLY -> date.year
        }

    private companion object {
        const val INITIAL_SEARCH_YEARS = 1L
        const val MAX_SEARCH_ATTEMPTS = 10
    }
}
