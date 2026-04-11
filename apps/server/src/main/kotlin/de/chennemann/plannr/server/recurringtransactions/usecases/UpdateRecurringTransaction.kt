package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.currencies.usecases.EnsureCurrencyExists
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.recurringtransactions.support.RecurringTransactionIdGenerator
import org.springframework.stereotype.Component

interface UpdateRecurringTransaction {
    suspend operator fun invoke(command: Command): RecurringTransaction

    data class Command(
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
}

@Component
internal class UpdateRecurringTransactionUseCase(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val ensureCurrencyExists: EnsureCurrencyExists,
    private val contextResolver: RecurringTransactionContextResolver,
    private val recurringTransactionIdGenerator: RecurringTransactionIdGenerator,
    private val timeProvider: TimeProvider,
    private val normalization: RecurringTransactionNormalization,
    private val versioningService: RecurringVersioningService,
) : UpdateRecurringTransaction {
    override suspend fun invoke(command: UpdateRecurringTransaction.Command): RecurringTransaction {
        val existing = recurringTransactionRepository.findById(command.id.trim())
            ?: throw NotFoundException("not_found", "Recurring transaction not found", mapOf("id" to command.id.trim()))
        val currency = ensureCurrencyExists(command.currencyCode)
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
                    currencyCode = currency.code,
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
                ),
            )
            "new_version" -> createNewVersion(existing, context, command, currency.code, normalizedRecurrence)
            else -> throw ValidationException("validation_error", "Recurring transaction update mode is invalid")
        }
    }

    private suspend fun createNewVersion(
        existing: RecurringTransaction,
        context: RecurringTransactionContextResolver.ResolvedContext,
        command: UpdateRecurringTransaction.Command,
        currencyCode: String,
        normalizedRecurrence: RecurringTransactionNormalization.NormalizedFields,
    ): RecurringTransaction {
        if (recurringTransactionRepository.findByPreviousVersionId(existing.id).isNotEmpty()) {
            throw ValidationException("validation_error", "Recurring transaction version chain already has a successor version")
        }
        val predecessorOccurrence = versioningService.predecessorOccurrence(existing, normalizedRecurrence.firstOccurrenceDate)
        recurringTransactionRepository.update(existing.copy(finalOccurrenceDate = predecessorOccurrence))
        return recurringTransactionRepository.save(
            RecurringTransaction(
                id = recurringTransactionIdGenerator(),
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
                lastMaterializedDate = null,
                previousVersionId = existing.id,
                isArchived = false,
                createdAt = timeProvider(),
            ),
        )
    }
}
