package de.chennemann.plannr.server.transactions.templates.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.transactions.materialization.service.MaterializationOperation
import de.chennemann.plannr.server.transactions.materialization.service.TransactionMaterializerService
import de.chennemann.plannr.server.transactions.templates.domain.RecurrencePattern
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplateRepository
import de.chennemann.plannr.server.transactions.templates.domain.save
import de.chennemann.plannr.server.transactions.templates.persistence.TransactionTemplateModel
import de.chennemann.plannr.server.transactions.templates.persistence.toDomain
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TransactionTemplateServiceImpl(
    private val transactionTemplateRepository: TransactionTemplateRepository,
    private val transactionMaterializerService: TransactionMaterializerService,
    private val timeProvider: TimeProvider,
) : TransactionTemplateService {
    override suspend fun create(command: TransactionTemplateService.CreateCommand): TransactionTemplate {
        val created = transactionTemplateRepository.save(
            TransactionTemplateModel(
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
        transactionMaterializerService.materialize(MaterializationOperation.NewTransactionTemplate(created))
        return created
    }

    override suspend fun update(command: TransactionTemplateService.UpdateCommand): TransactionTemplate {
        val existing = existingTransactionTemplate(command.id)
        val updated = transactionTemplateRepository.save(
            existing.copy(
                sourcePocketId = command.sourcePocketId,
                destinationPocketId = command.destinationPocketId,
                partnerId = command.partnerId,
                title = command.title,
                description = command.description,
                amount = command.amount,
                currencyCode = command.currencyCode,
                transactionType = command.transactionType,
                recurrencePattern = command.toRecurrencePattern(),
            ),
        )
        val operation = when {
            existing.sameScheduleExceptEndDate(updated) -> MaterializationOperation.EndDateChange(updated)
            existing.requiresFullRefresh(updated) -> MaterializationOperation.FullRefresh(updated)
            else -> MaterializationOperation.FullRefresh(updated)
        }
        transactionMaterializerService.materialize(operation)
        return updated
    }

    override suspend fun archive(id: Long): TransactionTemplate {
        val existing = existingTransactionTemplate(id)
        return transactionTemplateRepository.save(existing.copy(isArchived = true))
    }

    override suspend fun unarchive(id: Long): TransactionTemplate {
        val existing = existingTransactionTemplate(id)
        return transactionTemplateRepository.save(existing.copy(isArchived = false))
    }

    override suspend fun archiveForPocket(pocketId: Long) {
        transactionTemplateRepository
            .findAllBySourcePocketIdAndIsArchivedOrDestinationPocketIdAndIsArchivedOrderByCreatedAtAscIdAsc(
                sourcePocketId = pocketId,
                sourceIsArchived = false,
                destinationPocketId = pocketId,
                destinationIsArchived = false,
            )
            .toList()
            .map(TransactionTemplateModel::toDomain)
            .forEach { transactionTemplateRepository.save(it.copy(isArchived = true)) }
    }

    override suspend fun unarchiveForPocket(pocketId: Long) {
        transactionTemplateRepository
            .findAllBySourcePocketIdAndIsArchivedOrDestinationPocketIdAndIsArchivedOrderByCreatedAtAscIdAsc(
                sourcePocketId = pocketId,
                sourceIsArchived = true,
                destinationPocketId = pocketId,
                destinationIsArchived = true,
            )
            .toList()
            .map(TransactionTemplateModel::toDomain)
            .forEach { transactionTemplateRepository.save(it.copy(isArchived = false)) }
    }

    override suspend fun delete(id: Long) {
        existingTransactionTemplate(id)
        transactionTemplateRepository.deleteById(id)
    }

    override suspend fun list(archived: Boolean?): List<TransactionTemplate> {
        val models = if (archived == null) {
            transactionTemplateRepository.findAll().toList()
        } else {
            transactionTemplateRepository.findAllByIsArchivedOrderByCreatedAtAscIdAsc(archived).toList()
        }
        return models.map(TransactionTemplateModel::toDomain)
    }

    override suspend fun getById(id: Long): TransactionTemplate? =
        transactionTemplateRepository.findById(id)?.toDomain()

    private suspend fun existingTransactionTemplate(id: Long): TransactionTemplate =
        getById(id)
            ?: throw NotFoundException(
                code = "not_found",
                message = "Transaction template not found",
                details = mapOf("id" to id),
            )
}

private fun List<*>?.toCsv(): String? =
    this?.joinToString(",")?.takeIf { it.isNotBlank() }

private fun TransactionTemplateService.UpdateCommand.toRecurrencePattern(): RecurrencePattern =
    RecurrencePattern(
        firstOccurrenceDate = firstOccurrenceDate,
        finalOccurrenceDate = finalOccurrenceDate,
        recurrenceType = recurrenceType,
        skipCount = skipCount,
        daysOfWeek = daysOfWeek,
        weeksOfMonth = weeksOfMonth,
        daysOfMonth = daysOfMonth,
        monthsOfYear = monthsOfYear,
    )

private fun TransactionTemplate.sameScheduleExceptEndDate(other: TransactionTemplate): Boolean =
    recurrencePattern.copy(finalOccurrenceDate = null) == other.recurrencePattern.copy(finalOccurrenceDate = null) &&
        amount == other.amount &&
        currencyCode == other.currencyCode &&
        transactionType == other.transactionType &&
        sourcePocketId == other.sourcePocketId &&
        destinationPocketId == other.destinationPocketId &&
        partnerId == other.partnerId &&
        title == other.title &&
        description == other.description &&
        recurrencePattern.finalOccurrenceDate != other.recurrencePattern.finalOccurrenceDate

private fun TransactionTemplate.requiresFullRefresh(other: TransactionTemplate): Boolean =
    recurrencePattern != other.recurrencePattern ||
        amount != other.amount ||
        currencyCode != other.currencyCode ||
        transactionType != other.transactionType ||
        sourcePocketId != other.sourcePocketId ||
        destinationPocketId != other.destinationPocketId ||
        partnerId != other.partnerId ||
        title != other.title ||
        description != other.description
