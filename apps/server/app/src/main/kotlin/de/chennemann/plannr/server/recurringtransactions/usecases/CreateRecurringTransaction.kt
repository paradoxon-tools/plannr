package de.chennemann.plannr.server.recurringtransactions.usecases

import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.currencies.usecases.EnsureCurrencyExists
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.recurringtransactions.support.RecurringTransactionIdGenerator
import org.springframework.stereotype.Component

interface CreateRecurringTransaction {
    suspend operator fun invoke(command: Command): RecurringTransaction

    data class Command(
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
internal class CreateRecurringTransactionUseCase(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val ensureCurrencyExists: EnsureCurrencyExists,
    private val contextResolver: RecurringTransactionContextResolver,
    private val recurringTransactionIdGenerator: RecurringTransactionIdGenerator,
    private val timeProvider: TimeProvider,
    private val normalization: RecurringTransactionNormalization,
) : CreateRecurringTransaction {
    override suspend fun invoke(command: CreateRecurringTransaction.Command): RecurringTransaction {
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
        val recurringTransaction = RecurringTransaction(
            id = recurringTransactionIdGenerator(),
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
            lastMaterializedDate = null,
            previousVersionId = null,
            isArchived = false,
            createdAt = timeProvider(),
        )
        return recurringTransactionRepository.save(recurringTransaction)
    }
}
