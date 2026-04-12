package de.chennemann.plannr.server.transactions.recurring.usecases

import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.common.time.LocalDateProvider
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.recurrence.domain.RecurrenceCalculator
import de.chennemann.plannr.server.recurrence.domain.RecurrencePattern
import de.chennemann.plannr.server.query.projection.ProjectionDirtyScopeService
import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import de.chennemann.plannr.server.transactions.support.TransactionIdGenerator
import java.time.DayOfWeek
import java.time.LocalDate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class RecurringTransactionMaterializer(
    private val recurringTransactionRepository: de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val transactionIdGenerator: TransactionIdGenerator,
    private val localDateProvider: LocalDateProvider,
    private val timeProvider: TimeProvider,
    private val dirtyScopeService: ProjectionDirtyScopeService,
    private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculator(),
) {
    suspend fun materializeAll(): MaterializationSummary {
        val today = localDateProvider()
        val recurringTransactions = recurringTransactionRepository.findAll(archived = false)
        var createdCount = 0
        recurringTransactions.forEach { recurring ->
            createdCount += materialize(recurring, today)
        }
        return MaterializationSummary(createdCount)
    }

    suspend fun materialize(recurring: de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction, today: LocalDate = localDateProvider()): Int {
        if (recurring.isArchived) return 0

        val account = accountRepository.findById(recurring.accountId) ?: return 0
        val existingDates = transactionRepository.findByRecurringTransactionId(recurring.id)
            .map { it.transactionDate }
            .toMutableSet()
        val targets = calculateTargetDates(recurring, today)
        var createdCount = 0
        var latestCreatedDate: String? = null
        targets.forEach { occurrenceDate ->
            val materializedDate = applyWeekendHandling(occurrenceDate, account.weekendHandling)
            if (existingDates.add(materializedDate.toString())) {
                transactionRepository.save(
                    TransactionRecord(
                        id = transactionIdGenerator(),
                        accountId = recurring.accountId,
                        type = recurring.transactionType,
                        status = "PENDING",
                        transactionDate = materializedDate.toString(),
                        amount = recurring.amount,
                        currencyCode = recurring.currencyCode,
                        exchangeRate = null,
                        destinationAmount = null,
                        description = recurring.description ?: recurring.title,
                        partnerId = recurring.partnerId,
                        pocketId = if (recurring.transactionType == "TRANSFER") null else recurring.sourcePocketId ?: recurring.destinationPocketId,
                        sourcePocketId = recurring.sourcePocketId,
                        destinationPocketId = recurring.destinationPocketId,
                        parentTransactionId = null,
                        recurringTransactionId = recurring.id,
                        modifiedById = null,
                        transactionOrigin = "RECURRING_MATERIALIZED",
                        isArchived = false,
                        createdAt = timeProvider(),
                    ),
                )
                createdCount += 1
                latestCreatedDate = materializedDate.toString()
            }
        }
        if (latestCreatedDate != null) {
            recurringTransactionRepository.update(
                recurring.withLastMaterializedDate(
                    maxOf(recurring.lastMaterializedDate ?: latestCreatedDate, latestCreatedDate),
                ),
            )
            dirtyScopeService.markAccountDirty(recurring.accountId)
            setOfNotNull(recurring.sourcePocketId, recurring.destinationPocketId).forEach { dirtyScopeService.markPocketDirty(it) }
        }
        return createdCount
    }

    fun calculateTargetDates(recurring: de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction, today: LocalDate = localDateProvider()): List<LocalDate> {
        val pattern = recurring.toPattern()
        val endOfNextMonth = today.withDayOfMonth(1).plusMonths(2).minusDays(1)
        val throughNextMonth = recurrenceCalculator.occurrences(pattern, endInclusive = endOfNextMonth)
        val futureFive = recurrenceCalculator.occurrences(pattern).filter { it.isAfter(today) }.take(5)
        val horizon = listOfNotNull(throughNextMonth.lastOrNull(), futureFive.lastOrNull(), endOfNextMonth).maxOrNull() ?: endOfNextMonth
        return recurrenceCalculator.occurrences(pattern, endInclusive = horizon)
    }

    private fun de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction.toPattern(): RecurrencePattern =
        RecurrencePattern(
            firstOccurrenceDate = LocalDate.parse(firstOccurrenceDate),
            finalOccurrenceDate = finalOccurrenceDate?.let(LocalDate::parse),
            recurrenceType = de.chennemann.plannr.server.common.domain.RecurrenceType.valueOf(recurrenceType),
            skipCount = skipCount,
            daysOfWeek = daysOfWeek?.map(DayOfWeek::valueOf),
            weeksOfMonth = weeksOfMonth,
            daysOfMonth = daysOfMonth,
            monthsOfYear = monthsOfYear,
        )

    private fun applyWeekendHandling(date: LocalDate, weekendHandling: String): LocalDate = when {
        date.dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> date
        weekendHandling == "NO_SHIFT" -> date
        weekendHandling == "MOVE_BEFORE" -> when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> date.minusDays(1)
            DayOfWeek.SUNDAY -> date.minusDays(2)
            else -> date
        }
        weekendHandling == "MOVE_AFTER" -> when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> date.plusDays(2)
            DayOfWeek.SUNDAY -> date.plusDays(1)
            else -> date
        }
        else -> date
    }

    data class MaterializationSummary(val createdCount: Int)
}
