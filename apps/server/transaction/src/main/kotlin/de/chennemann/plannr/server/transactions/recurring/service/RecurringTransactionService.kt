package de.chennemann.plannr.server.transactions.recurring.service

import de.chennemann.plannr.server.common.domain.normalizeCurrency
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.persistence.RecurringTransactionModel
import de.chennemann.plannr.server.transactions.recurring.persistence.toModel
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecurringTransactionServiceImpl(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val contextResolver: RecurringTransactionContextResolver,
    private val timeProvider: TimeProvider,
    private val normalization: RecurringTransactionNormalization,
    private val versioningService: RecurringVersioningService,
) : RecurringTransactionService {
    @Transactional
    override suspend fun create(command: RecurringTransactionService.CreateCommand): RecurringTransaction {
        val currencyCode = normalizeCurrency(command.currencyCode)
        val context = contextResolver.resolve(command.sourcePocketId, command.destinationPocketId, command.partnerId, command.transactionType)
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
                previousVersionId = null,
                isArchived = false,
                createdAt = timeProvider(),
            ),
        )
    }

    @Transactional
    override suspend fun update(command: RecurringTransactionService.UpdateCommand): RecurringTransaction {
        val existing = recurringTransactionRepository.findById(command.id.trim())
            ?: throw NotFoundException("not_found", "Recurring transaction not found", mapOf("id" to command.id.trim()))
        val currencyCode = normalizeCurrency(command.currencyCode)
        val context = contextResolver.resolve(command.sourcePocketId, command.destinationPocketId, command.partnerId, command.transactionType)
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
    override suspend fun archive(id: String): RecurringTransaction {
        val existing = recurringTransactionRepository.findById(id.trim())
            ?: throw NotFoundException("not_found", "Recurring transaction not found", mapOf("id" to id.trim()))
        val updated = existing.archive()
        return recurringTransactionRepository.update(updated.toModel())
    }

    @Transactional
    override suspend fun unarchive(id: String): RecurringTransaction {
        val existing = recurringTransactionRepository.findById(id.trim())
            ?: throw NotFoundException("not_found", "Recurring transaction not found", mapOf("id" to id.trim()))
        val updated = existing.unarchive()
        return recurringTransactionRepository.update(updated.toModel())
    }

    @Transactional
    override suspend fun archiveForAccount(accountId: Long) {
        recurringTransactionRepository.findAll(accountId = accountId, archived = false)
            .forEach { recurringTransactionRepository.update(it.archive().toModel()) }
    }

    @Transactional
    override suspend fun unarchiveForAccount(accountId: Long) {
        recurringTransactionRepository.findAll(accountId = accountId, archived = true)
            .forEach { recurringTransactionRepository.update(it.unarchive().toModel()) }
    }

    @Transactional
    override suspend fun archiveForPocket(accountId: Long, pocketId: Long) {
        recurringTransactionRepository.findAll(accountId = accountId, archived = false)
            .filter { it.sourcePocketId == pocketId || it.destinationPocketId == pocketId }
            .forEach { recurringTransactionRepository.update(it.archive().toModel()) }
    }

    @Transactional
    override suspend fun unarchiveForPocket(accountId: Long, pocketId: Long) {
        recurringTransactionRepository.findAll(accountId = accountId, archived = true)
            .filter { it.sourcePocketId == pocketId || it.destinationPocketId == pocketId }
            .forEach { recurringTransactionRepository.update(it.unarchive().toModel()) }
    }

    @Transactional
    override suspend fun delete(id: String) {
        val normalizedId = id.trim()
        if (recurringTransactionRepository.findById(normalizedId) == null) {
            throw NotFoundException("not_found", "Recurring transaction not found", mapOf("id" to normalizedId))
        }
        recurringTransactionRepository.deleteById(normalizedId)
    }

    private suspend fun createNewVersion(
        existing: RecurringTransaction,
        context: RecurringTransactionContextResolver.ResolvedContext,
        command: RecurringTransactionService.UpdateCommand,
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
                previousVersionId = existing.id,
                isArchived = false,
                createdAt = timeProvider(),
            ),
        )
    }

}
