package de.chennemann.plannr.server.transactions.recurring.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransaction
import de.chennemann.plannr.server.transactions.recurring.domain.RecurringTransactionRepository
import de.chennemann.plannr.server.transactions.recurring.domain.save
import de.chennemann.plannr.server.transactions.recurring.persistence.RecurringTransactionModel
import de.chennemann.plannr.server.transactions.recurring.persistence.toDomain
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RecurringTransactionServiceImpl(
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val timeProvider: TimeProvider,
) : RecurringTransactionService {
    override suspend fun create(command: RecurringTransactionService.CreateCommand): RecurringTransaction =
        recurringTransactionRepository.save(
            RecurringTransactionModel(
                id = null,
                sourcePocketId = command.sourcePocketId,
                destinationPocketId = command.destinationPocketId,
                partnerId = command.partnerId,
                title = command.title,
                description = command.description,
                amount = command.amount,
                currencyCode = command.currencyCode,
                transactionType = command.transactionType,
                firstOccurrenceDate = command.firstOccurrenceDate,
                finalOccurrenceDate = command.finalOccurrenceDate,
                recurrenceType = command.recurrenceType,
                skipCount = command.skipCount,
                daysOfWeek = command.daysOfWeek.toCsv(),
                weeksOfMonth = command.weeksOfMonth.toCsv(),
                daysOfMonth = command.daysOfMonth.toCsv(),
                monthsOfYear = command.monthsOfYear.toCsv(),
                previousVersionId = null,
                isArchived = false,
                createdAt = timeProvider(),
            ),
        ).toDomain()

    override suspend fun update(command: RecurringTransactionService.UpdateCommand): RecurringTransaction {
        val existing = existingRecurringTransaction(command.id)
        return recurringTransactionRepository.save(
            existing.copy(
                sourcePocketId = command.sourcePocketId,
                destinationPocketId = command.destinationPocketId,
                partnerId = command.partnerId,
                title = command.title,
                description = command.description,
                amount = command.amount,
                currencyCode = command.currencyCode,
                transactionType = command.transactionType,
                firstOccurrenceDate = command.firstOccurrenceDate,
                finalOccurrenceDate = command.finalOccurrenceDate,
                recurrenceType = command.recurrenceType,
                skipCount = command.skipCount,
                daysOfWeek = command.daysOfWeek,
                weeksOfMonth = command.weeksOfMonth,
                daysOfMonth = command.daysOfMonth,
                monthsOfYear = command.monthsOfYear,
            ),
        )
    }

    override suspend fun archive(id: Long): RecurringTransaction {
        val existing = existingRecurringTransaction(id)
        return recurringTransactionRepository.save(existing.copy(isArchived = true))
    }

    override suspend fun unarchive(id: Long): RecurringTransaction {
        val existing = existingRecurringTransaction(id)
        return recurringTransactionRepository.save(existing.copy(isArchived = false))
    }

    override suspend fun archiveForPocket(pocketId: Long) {
        recurringTransactionRepository
            .findAllBySourcePocketIdAndIsArchivedOrDestinationPocketIdAndIsArchivedOrderByCreatedAtAscIdAsc(
                sourcePocketId = pocketId,
                sourceIsArchived = false,
                destinationPocketId = pocketId,
                destinationIsArchived = false,
            )
            .toList()
            .map(RecurringTransactionModel::toDomain)
            .forEach { recurringTransactionRepository.save(it.copy(isArchived = true)) }
    }

    override suspend fun unarchiveForPocket(pocketId: Long) {
        recurringTransactionRepository
            .findAllBySourcePocketIdAndIsArchivedOrDestinationPocketIdAndIsArchivedOrderByCreatedAtAscIdAsc(
                sourcePocketId = pocketId,
                sourceIsArchived = true,
                destinationPocketId = pocketId,
                destinationIsArchived = true,
            )
            .toList()
            .map(RecurringTransactionModel::toDomain)
            .forEach { recurringTransactionRepository.save(it.copy(isArchived = false)) }
    }

    override suspend fun delete(id: Long) {
        existingRecurringTransaction(id)
        recurringTransactionRepository.deleteById(id)
    }

    override suspend fun list(archived: Boolean?): List<RecurringTransaction> {
        val models = if (archived == null) {
            recurringTransactionRepository.findAll().toList()
        } else {
            recurringTransactionRepository.findAllByIsArchivedOrderByCreatedAtAscIdAsc(archived).toList()
        }
        return models.map(RecurringTransactionModel::toDomain)
    }

    override suspend fun getById(id: Long): RecurringTransaction? =
        recurringTransactionRepository.findById(id)?.toDomain()

    private suspend fun existingRecurringTransaction(id: Long): RecurringTransaction =
        getById(id)
            ?: throw NotFoundException(
                code = "not_found",
                message = "Recurring transaction not found",
                details = mapOf("id" to id),
            )
}

private fun List<*>?.toCsv(): String? =
    this?.joinToString(",")?.takeIf { it.isNotBlank() }
