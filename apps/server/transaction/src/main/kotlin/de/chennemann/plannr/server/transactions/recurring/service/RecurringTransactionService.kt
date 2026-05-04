package de.chennemann.plannr.server.transactions.recurring.service

import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.common.domain.RecurrenceType
import de.chennemann.plannr.server.common.domain.normalizeCurrency
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.common.time.LocalDateProvider
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import de.chennemann.plannr.server.transactions.persistence.TransactionModel
import de.chennemann.plannr.server.transactions.recurring.domain.RecurrenceCalculator
import de.chennemann.plannr.server.transactions.recurring.domain.RecurrencePattern
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.persistence.RecurringTransactionModel
import de.chennemann.plannr.server.transactions.recurring.persistence.toModel
import java.time.DayOfWeek
import java.time.LocalDate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecurringTransactionService(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val transactionRepository: TransactionRepository,
    private val accountService: AccountService,
    private val contextResolver: RecurringTransactionContextResolver,
    private val timeProvider: TimeProvider,
    private val localDateProvider: LocalDateProvider,
    private val normalization: RecurringTransactionNormalization,
    private val versioningService: RecurringVersioningService,
    private val projectionPort: RecurringTransactionProjectionPort,
    private val recurrenceCalculator: RecurrenceCalculator = RecurrenceCalculator(),
) {
    @Transactional
    suspend fun create(command: CreateCommand): RecurringTransaction {
        val currencyCode = normalizeCurrency(command.currencyCode)
        val context = contextResolver.resolve(command.contractId, command.sourcePocketId, command.destinationPocketId, command.partnerId, command.transactionType)
        val normalizedRecurrence = normalization.normalize(
            RecurringTransactionNormalization.Fields(
                firstOccurrenceDate = command.firstOccurrenceDate,
                finalOccurrenceDate = command.finalOccurrenceDate,
                recurrenceType = command.recurrenceType,
                skipCount = command.skipCount,
                daysOfWeek = command.daysOfWeek,
                weeksOfMonth = command.weeksOfMonth,
                daysOfMonth = command.daysOfMonth,
                monthsOfYear = command.monthsOfYear,
                maxRecurrenceCount = command.maxRecurrenceCount,
            ),
        )
        return recurringTransactionRepository.save(
            RecurringTransactionModel(
                id = null,
                sourcePocketId = context.sourcePocketId,
                destinationPocketId = context.destinationPocketId,
                partnerId = context.partnerId,
                title = command.title,
                description = command.description,
                amount = command.amount,
                currencyCode = currencyCode,
                transactionType = command.transactionType,
                firstOccurrenceDate = normalizedRecurrence.firstOccurrenceDate,
                finalOccurrenceDate = normalizedRecurrence.finalOccurrenceDate,
                recurrenceType = command.recurrenceType,
                skipCount = command.skipCount,
                daysOfWeek = command.daysOfWeek,
                weeksOfMonth = command.weeksOfMonth,
                daysOfMonth = command.daysOfMonth,
                monthsOfYear = command.monthsOfYear,
                lastMaterializedDate = null,
                previousVersionId = null,
                isArchived = false,
                createdAt = timeProvider(),
            ),
        )
    }

    @Transactional
    suspend fun update(command: UpdateCommand): RecurringTransaction {
        val existing = recurringTransactionRepository.findById(command.id.trim())
            ?: throw NotFoundException("not_found", "Recurring transaction not found", mapOf("id" to command.id.trim()))
        val currencyCode = normalizeCurrency(command.currencyCode)
        val context = contextResolver.resolve(command.contractId, command.sourcePocketId, command.destinationPocketId, command.partnerId, command.transactionType)
        val normalizedRecurrence = normalization.normalize(
            RecurringTransactionNormalization.Fields(
                firstOccurrenceDate = command.firstOccurrenceDate,
                finalOccurrenceDate = command.finalOccurrenceDate,
                recurrenceType = command.recurrenceType,
                skipCount = command.skipCount,
                daysOfWeek = command.daysOfWeek,
                weeksOfMonth = command.weeksOfMonth,
                daysOfMonth = command.daysOfMonth,
                monthsOfYear = command.monthsOfYear,
                maxRecurrenceCount = command.maxRecurrenceCount,
            ),
        )
        val mode = command.updateMode.trim().lowercase()

        return when (mode) {
            "overwrite" -> recurringTransactionRepository.update(
                RecurringTransaction(
                    id = existing.id,
                    contractId = context.contractId,
                    accountId = context.accountId,
                    sourcePocketId = context.sourcePocketId,
                    destinationPocketId = context.destinationPocketId,
                    partnerId = context.partnerId,
                    title = command.title,
                    description = command.description,
                    amount = command.amount,
                    currencyCode = currencyCode,
                    transactionType = command.transactionType,
                    firstOccurrenceDate = normalizedRecurrence.firstOccurrenceDate,
                    finalOccurrenceDate = normalizedRecurrence.finalOccurrenceDate,
                    recurrenceType = command.recurrenceType,
                    skipCount = command.skipCount,
                    daysOfWeek = command.daysOfWeek,
                    weeksOfMonth = command.weeksOfMonth,
                    daysOfMonth = command.daysOfMonth,
                    monthsOfYear = command.monthsOfYear,
                    lastMaterializedDate = existing.lastMaterializedDate,
                    previousVersionId = existing.previousVersionId,
                    isArchived = existing.isArchived,
                    createdAt = existing.createdAt,
                ).toModel(),
            )
            "new_version" -> createNewVersion(existing, context, command, currencyCode, normalizedRecurrence)
            else -> throw ValidationException("validation_error", "Recurring transaction update mode is invalid")
        }
    }

    @Transactional
    suspend fun archive(id: String): RecurringTransaction {
        val existing = recurringTransactionRepository.findById(id.trim())
            ?: throw NotFoundException("not_found", "Recurring transaction not found", mapOf("id" to id.trim()))
        val updated = existing.archive()
        return recurringTransactionRepository.update(updated.toModel())
    }

    @Transactional
    suspend fun unarchive(id: String): RecurringTransaction {
        val existing = recurringTransactionRepository.findById(id.trim())
            ?: throw NotFoundException("not_found", "Recurring transaction not found", mapOf("id" to id.trim()))
        val updated = existing.unarchive()
        return recurringTransactionRepository.update(updated.toModel())
    }

    @Transactional
    suspend fun materializeAll(): MaterializationSummary {
        val today = localDateProvider()
        val recurringTransactions = recurringTransactionRepository.findAll(archived = false)
        var createdCount = 0
        recurringTransactions.forEach { recurring ->
            createdCount += materialize(recurring, today)
        }
        return MaterializationSummary(createdCount)
    }

    @Transactional
    suspend fun materialize(recurring: RecurringTransaction, today: LocalDate = localDateProvider()): Int {
        if (recurring.isArchived) return 0

        val account = accountService.getById(recurring.accountId) ?: return 0
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
                    TransactionModel(
                        id = null,
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
                ).toModel(),
            )
            projectionPort.markAccountDirty(recurring.accountId)
            setOfNotNull(recurring.sourcePocketId, recurring.destinationPocketId).forEach { projectionPort.markPocketDirty(it) }
        }
        return createdCount
    }

    fun calculateTargetDates(recurring: RecurringTransaction, today: LocalDate = localDateProvider()): List<LocalDate> {
        val pattern = recurring.toPattern()
        val endOfNextMonth = today.withDayOfMonth(1).plusMonths(2).minusDays(1)
        val throughNextMonth = recurrenceCalculator.occurrences(pattern, endInclusive = endOfNextMonth)
        val futureFive = recurrenceCalculator.occurrences(pattern).filter { it.isAfter(today) }.take(5)
        val horizon = listOfNotNull(throughNextMonth.lastOrNull(), futureFive.lastOrNull(), endOfNextMonth).maxOrNull() ?: endOfNextMonth
        return recurrenceCalculator.occurrences(pattern, endInclusive = horizon)
    }

    private suspend fun createNewVersion(
        existing: RecurringTransaction,
        context: RecurringTransactionContextResolver.ResolvedContext,
        command: UpdateCommand,
        currencyCode: String,
        normalizedRecurrence: RecurringTransactionNormalization.NormalizedFields,
    ): RecurringTransaction {
        if (recurringTransactionRepository.findByPreviousVersionId(existing.id).isNotEmpty()) {
            throw ValidationException("validation_error", "Recurring transaction version chain already has a successor version")
        }
        val predecessorOccurrence = versioningService.predecessorOccurrence(existing, normalizedRecurrence.firstOccurrenceDate)
        recurringTransactionRepository.update(existing.withFinalOccurrenceDate(predecessorOccurrence).toModel())
        return recurringTransactionRepository.save(
            RecurringTransactionModel(
                id = null,
                sourcePocketId = context.sourcePocketId,
                destinationPocketId = context.destinationPocketId,
                partnerId = context.partnerId,
                title = command.title,
                description = command.description,
                amount = command.amount,
                currencyCode = currencyCode,
                transactionType = command.transactionType,
                firstOccurrenceDate = normalizedRecurrence.firstOccurrenceDate,
                finalOccurrenceDate = normalizedRecurrence.finalOccurrenceDate,
                recurrenceType = command.recurrenceType,
                skipCount = command.skipCount,
                daysOfWeek = command.daysOfWeek,
                weeksOfMonth = command.weeksOfMonth,
                daysOfMonth = command.daysOfMonth,
                monthsOfYear = command.monthsOfYear,
                lastMaterializedDate = null,
                previousVersionId = existing.id,
                isArchived = false,
                createdAt = timeProvider(),
            ),
        )
    }

    private fun RecurringTransaction.toPattern(): RecurrencePattern =
        RecurrencePattern(
            firstOccurrenceDate = LocalDate.parse(firstOccurrenceDate),
            finalOccurrenceDate = finalOccurrenceDate?.let(LocalDate::parse),
            recurrenceType = RecurrenceType.valueOf(recurrenceType),
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

    data class CreateCommand(
        val contractId: String?,
        val sourcePocketId: String?,
        val destinationPocketId: String?,
        val partnerId: String?,
        val title: String,
        val description: String?,
        val amount: Long,
        val currencyCode: String,
        val transactionType: String,
        val firstOccurrenceDate: String,
        val finalOccurrenceDate: String?,
        val recurrenceType: String,
        val skipCount: Int,
        val daysOfWeek: List<String>?,
        val weeksOfMonth: List<Int>?,
        val daysOfMonth: List<Int>?,
        val monthsOfYear: List<Int>?,
        val maxRecurrenceCount: Int?,
    )

    data class UpdateCommand(
        val id: String,
        val updateMode: String,
        val contractId: String?,
        val sourcePocketId: String?,
        val destinationPocketId: String?,
        val partnerId: String?,
        val title: String,
        val description: String?,
        val amount: Long,
        val currencyCode: String,
        val transactionType: String,
        val firstOccurrenceDate: String,
        val finalOccurrenceDate: String?,
        val recurrenceType: String,
        val skipCount: Int,
        val daysOfWeek: List<String>?,
        val weeksOfMonth: List<Int>?,
        val daysOfMonth: List<Int>?,
        val monthsOfYear: List<Int>?,
        val maxRecurrenceCount: Int?,
    )

    data class MaterializationSummary(val createdCount: Int)
}
