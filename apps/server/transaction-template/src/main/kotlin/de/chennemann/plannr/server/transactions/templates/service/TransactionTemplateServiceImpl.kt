package de.chennemann.plannr.server.transactions.templates.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.UpdateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.materialization.service.MaterializationOperation
import de.chennemann.plannr.server.transactions.materialization.service.TransactionMaterializerService
import de.chennemann.plannr.server.transactions.materialization.service.UpcomingTransactionCache
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionChangeEvent
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionEventQueue
import de.chennemann.plannr.server.transactions.templates.domain.RecurrencePattern
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplateRepository
import de.chennemann.plannr.server.transactions.templates.domain.save
import de.chennemann.plannr.server.transactions.templates.persistence.TransactionTemplateModel
import de.chennemann.plannr.server.transactions.templates.persistence.toDTO
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class TransactionTemplateServiceImpl(
    private val transactionTemplateRepository: TransactionTemplateRepository,
    private val transactionMaterializerService: TransactionMaterializerService,
    private val timeProvider: TimeProvider,
    private val projectionEventQueue: TransactionProjectionEventQueue? = null,
    private val upcomingTransactionCache: UpcomingTransactionCache? = null,
) : TransactionTemplateService {
    override suspend fun create(command: CreateTransactionTemplateCommand): TransactionTemplate {
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
        ).toDTO()
        transactionMaterializerService.materialize(MaterializationOperation.NewTransactionTemplate(created))
        upcomingTransactionCache?.refresh(created)
        enqueueProjectionChange(created.id)
        return created
    }

    override suspend fun createBatch(commands: List<CreateTransactionTemplateCommand>): List<TransactionTemplate> {
        if (commands.isEmpty()) {
            throw ValidationException(
                code = "validation_error",
                message = "At least one transaction template is required",
                details = mapOf("field" to "templates"),
            )
        }
        return commands.map { create(it) }
    }

    override suspend fun update(command: UpdateTransactionTemplateCommand): TransactionTemplate {
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
        upcomingTransactionCache?.refresh(updated)
        enqueueProjectionChange(updated.id)
        return updated
    }

    override suspend fun archive(id: Long): TransactionTemplate {
        val existing = existingTransactionTemplate(id)
        val archived = transactionTemplateRepository.save(existing.copy(isArchived = true))
        upcomingTransactionCache?.invalidate(archived.id)
        enqueueProjectionChange(archived.id)
        return archived
    }

    override suspend fun unarchive(id: Long): TransactionTemplate {
        val existing = existingTransactionTemplate(id)
        val unarchived = transactionTemplateRepository.save(existing.copy(isArchived = false))
        upcomingTransactionCache?.refresh(unarchived)
        enqueueProjectionChange(unarchived.id)
        return unarchived
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
            .map(TransactionTemplateModel::toDTO)
            .forEach {
                val archived = transactionTemplateRepository.save(it.copy(isArchived = true))
                upcomingTransactionCache?.invalidate(archived.id)
                enqueueProjectionChange(archived.id)
            }
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
            .map(TransactionTemplateModel::toDTO)
            .forEach {
                val unarchived = transactionTemplateRepository.save(it.copy(isArchived = false))
                upcomingTransactionCache?.refresh(unarchived)
                enqueueProjectionChange(unarchived.id)
            }
    }

    override suspend fun delete(id: Long) {
        existingTransactionTemplate(id)
        transactionTemplateRepository.deleteById(id)
        upcomingTransactionCache?.invalidate(id)
        enqueueProjectionChange(id)
    }

    override suspend fun list(archived: Boolean?): List<TransactionTemplate> {
        val models = if (archived == null) {
            transactionTemplateRepository.findAll().toList()
        } else {
            transactionTemplateRepository.findAllByIsArchivedOrderByCreatedAtAscIdAsc(archived).toList()
        }
        return models.map(TransactionTemplateModel::toDTO)
    }

    override suspend fun getById(id: Long): TransactionTemplate? =
        transactionTemplateRepository.findById(id)?.toDTO()

    private suspend fun existingTransactionTemplate(id: Long): TransactionTemplate =
        getById(id)
            ?: throw NotFoundException(
                code = "not_found",
                message = "Transaction template not found",
                details = mapOf("id" to id),
            )

    private suspend fun enqueueProjectionChange(id: Long) {
        projectionEventQueue?.enqueue(
            TransactionProjectionChangeEvent.TransactionTemplateChanged(id),
        )
    }
}

private fun List<*>?.toCsv(): String? =
    this?.joinToString(",")?.takeIf { it.isNotBlank() }

private fun UpdateTransactionTemplateCommand.toRecurrencePattern(): RecurrencePattern =
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
