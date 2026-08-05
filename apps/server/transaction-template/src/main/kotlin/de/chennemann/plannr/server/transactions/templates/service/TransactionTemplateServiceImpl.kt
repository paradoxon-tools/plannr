package de.chennemann.plannr.server.transactions.templates.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.contracts.service.ContractService
import de.chennemann.plannr.server.financialprofiles.service.FinancialProfileService
import de.chennemann.plannr.server.transactions.materialization.service.MaterializationOperation
import de.chennemann.plannr.server.transactions.materialization.service.TransactionMaterializerService
import de.chennemann.plannr.server.transactions.materialization.service.UpcomingTransactionCache
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionChangeEvent
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionEventQueue
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateVersionCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateWithVersionsCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.UpdateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.domain.EffectiveTransactionTemplate
import de.chennemann.plannr.server.transactions.templates.domain.RecurrencePattern
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplateRepository
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplateVersion
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplateVersionRepository
import de.chennemann.plannr.server.transactions.templates.persistence.TransactionTemplateModel
import de.chennemann.plannr.server.transactions.templates.persistence.TransactionTemplateVersionModel
import de.chennemann.plannr.server.transactions.templates.persistence.toCsv
import de.chennemann.plannr.server.transactions.templates.persistence.toDomain
import de.chennemann.plannr.server.transactions.templates.persistence.toModel
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class TransactionTemplateServiceImpl(
    private val transactionTemplateRepository: TransactionTemplateRepository,
    private val transactionTemplateVersionRepository: TransactionTemplateVersionRepository,
    private val transactionMaterializerService: TransactionMaterializerService,
    private val financialProfileService: FinancialProfileService,
    private val contractService: ContractService,
    private val timeProvider: TimeProvider,
    private val projectionEventQueue: TransactionProjectionEventQueue? = null,
    private val upcomingTransactionCache: UpcomingTransactionCache? = null,
) : TransactionTemplateService {
    override suspend fun create(command: CreateTransactionTemplateCommand): TransactionTemplate =
        createAggregate(command.toBatchCommand())

    override suspend fun createBatch(commands: List<CreateTransactionTemplateWithVersionsCommand>): List<TransactionTemplate> {
        if (commands.isEmpty()) throw validation("At least one transaction template is required", "templates")
        commands.forEachIndexed(::validateTimeline)
        return commands.map { createAggregate(it) }
    }

    override suspend fun createVersion(
        transactionTemplateId: Long,
        command: CreateTransactionTemplateVersionCommand,
    ): TransactionTemplate {
        val template = existingTransactionTemplate(transactionTemplateId)
        val latest = template.currentVersion
        val start = parseDate(command.firstOccurrenceDate, "firstOccurrenceDate")
        requireAfter(start, parseDate(latest.validFrom, "validFrom"))
        persistAndRefresh(template, latest.copy(validUntil = start.minusDays(1).toString()), MaterializationOperation::EndDateChange)
        val created = saveVersion(transactionTemplateId, command, null)
        refresh(template, created, MaterializationOperation::NewTransactionTemplate)
        enqueueProjectionChange(transactionTemplateId)
        return existingTransactionTemplate(transactionTemplateId)
    }

    override suspend fun update(command: UpdateTransactionTemplateCommand): TransactionTemplate {
        val existingVersion = existingVersion(command.id)
        val template = existingTransactionTemplate(existingVersion.transactionTemplateId)
        val index = template.versions.indexOfFirst { it.id == existingVersion.id }
        val start = parseDate(command.firstOccurrenceDate, "firstOccurrenceDate")
        val previous = template.versions.getOrNull(index - 1)
        val next = template.versions.getOrNull(index + 1)
        previous?.let { requireAfter(start, parseDate(it.validFrom, "validFrom")) }
        next?.let { requireAfter(parseDate(it.validFrom, "validFrom"), start) }

        val profileId = resolveFinancialProfileId(command.contractId, command.financialProfileId)
        transactionTemplateRepository.save(
            template.toModel().copy(
                contractId = command.contractId,
                sourcePocketId = command.sourcePocketId,
                destinationPocketId = command.destinationPocketId,
                financialProfileId = profileId,
                partnerId = command.partnerId,
                title = command.title,
                description = command.description,
                currencyCode = command.currencyCode,
                transactionType = command.transactionType,
            ),
        )
        val correctedTemplate = existingTransactionTemplate(template.id)
        val updatedVersion = existingVersion.copy(
            amount = command.amount,
            recurrencePattern = command.toRecurrencePattern(),
            validFrom = start.toString(),
            validUntil = next?.validFrom?.let { parseDate(it, "validFrom").minusDays(1).toString() },
        )
        persistAndRefresh(correctedTemplate, updatedVersion, MaterializationOperation::FullRefresh)
        previous?.let {
            persistAndRefresh(correctedTemplate, it.copy(validUntil = start.minusDays(1).toString()), MaterializationOperation::EndDateChange)
        }
        enqueueProjectionChange(template.id)
        return existingTransactionTemplate(template.id)
    }

    override suspend fun archive(id: Long) = setArchived(id, true)
    override suspend fun unarchive(id: Long) = setArchived(id, false)
    override suspend fun archiveForPocket(pocketId: Long) = setArchivedForPocket(pocketId, true)
    override suspend fun unarchiveForPocket(pocketId: Long) = setArchivedForPocket(pocketId, false)

    override suspend fun refreshFinancialProfilesForPocket(pocketId: Long) {
        transactionTemplateRepository.findAll().toList()
            .filter { it.sourcePocketId == pocketId || it.destinationPocketId == pocketId }
            .forEach { refreshFinancialProfile(it) }
    }

    override suspend fun refreshFinancialProfilesForContract(contractId: Long) {
        transactionTemplateRepository.findAll().toList().filter { it.contractId == contractId }.forEach { refreshFinancialProfile(it) }
    }

    override suspend fun delete(id: Long) {
        val template = existingTransactionTemplate(id)
        transactionTemplateRepository.deleteById(id)
        template.versions.forEach { upcomingTransactionCache?.invalidate(it.id) }
        enqueueProjectionChange(id)
    }

    override suspend fun deleteVersion(transactionTemplateId: Long, versionId: Long): TransactionTemplate? {
        val template = existingTransactionTemplate(transactionTemplateId)
        val index = template.versions.indexOfFirst { it.id == versionId }
        if (index < 0) throw notFound("Transaction template version", versionId)
        if (template.versions.size == 1) {
            delete(transactionTemplateId)
            return null
        }
        transactionTemplateVersionRepository.deleteById(versionId)
        upcomingTransactionCache?.invalidate(versionId)
        template.versions.getOrNull(index - 1)?.let { previous ->
            val next = template.versions.getOrNull(index + 1)
            persistAndRefresh(
                template,
                previous.copy(validUntil = next?.validFrom?.let { parseDate(it, "validFrom").minusDays(1).toString() }),
                MaterializationOperation::EndDateChange,
            )
        }
        enqueueProjectionChange(transactionTemplateId)
        return existingTransactionTemplate(transactionTemplateId)
    }

    override suspend fun list(archived: Boolean?): List<TransactionTemplate> {
        val models = if (archived == null) transactionTemplateRepository.findAll().toList()
        else transactionTemplateRepository.findAllByIsArchivedOrderByCreatedAtAscIdAsc(archived).toList()
        return models.map { it.toDomainAggregate() }
    }

    override suspend fun getById(id: Long) = transactionTemplateRepository.findById(id)?.toDomainAggregate()

    private suspend fun createAggregate(command: CreateTransactionTemplateWithVersionsCommand): TransactionTemplate {
        validateTimeline(0, command)
        val createdAt = timeProvider()
        val profileId = resolveFinancialProfileId(command.contractId, command.financialProfileId)
        val model = transactionTemplateRepository.save(
            TransactionTemplateModel(
                null, command.contractId, command.sourcePocketId, command.destinationPocketId, profileId,
                command.partnerId, command.title, command.description, command.currencyCode,
                command.transactionType, false, createdAt,
            ),
        )
        val templateId = requireNotNull(model.id)
        var template = model.toDomainAggregate()
        command.versions.forEachIndexed { index, versionCommand ->
            val validUntil = command.versions.getOrNull(index + 1)?.firstOccurrenceDate
                ?.let { parseDate(it, "firstOccurrenceDate").minusDays(1).toString() }
            val version = saveVersion(templateId, versionCommand, validUntil)
            template = template.copy(versions = template.versions + version)
            refresh(template, version, MaterializationOperation::NewTransactionTemplate)
        }
        enqueueProjectionChange(templateId)
        return template
    }

    private suspend fun saveVersion(
        templateId: Long,
        command: CreateTransactionTemplateVersionCommand,
        validUntil: String?,
    ) = transactionTemplateVersionRepository.save(
        TransactionTemplateVersionModel(
            null, templateId, command.amount, command.firstOccurrenceDate, command.finalOccurrenceDate,
            command.recurrenceType, command.skipCount, command.daysOfWeek.toCsv(), command.weeksOfMonth.toCsv(),
            command.daysOfMonth.toCsv(), command.monthsOfYear.toCsv(), command.firstOccurrenceDate, validUntil, timeProvider(),
        ),
    ).toDomain()

    private suspend fun TransactionTemplateModel.toDomainAggregate() = TransactionTemplate(
        requireNotNull(id), contractId, sourcePocketId, destinationPocketId, financialProfileId,
        partnerId, title, description, currencyCode, transactionType,
        transactionTemplateVersionRepository.findAllByTransactionTemplateIdOrderByValidFromAscIdAsc(requireNotNull(id))
            .toList().map(TransactionTemplateVersionModel::toDomain),
        isArchived, createdAt,
    )

    private fun TransactionTemplate.toModel() = TransactionTemplateModel(
        id, contractId, sourcePocketId, destinationPocketId, financialProfileId, partnerId,
        title, description, currencyCode, transactionType, isArchived, createdAt,
    )

    private suspend fun existingTransactionTemplate(id: Long) = getById(id) ?: throw notFound("Transaction template", id)
    private suspend fun existingVersion(id: Long) = transactionTemplateVersionRepository.findById(id)?.toDomain()
        ?: throw notFound("Transaction template version", id)

    private suspend fun setArchived(id: Long, archived: Boolean): TransactionTemplate {
        val template = existingTransactionTemplate(id)
        transactionTemplateRepository.save(template.toModel().copy(isArchived = archived))
        template.versions.forEach { if (archived) upcomingTransactionCache?.invalidate(it.id) else upcomingTransactionCache?.refresh(EffectiveTransactionTemplate(template, it)) }
        enqueueProjectionChange(id)
        return template.copy(isArchived = archived)
    }

    private suspend fun setArchivedForPocket(pocketId: Long, archived: Boolean) {
        transactionTemplateRepository.findAll().toList()
            .filter { it.sourcePocketId == pocketId || it.destinationPocketId == pocketId }
            .forEach { setArchived(requireNotNull(it.id), archived) }
    }

    private suspend fun refreshFinancialProfile(model: TransactionTemplateModel) {
        val resolved = resolveFinancialProfileId(model.contractId, model.financialProfileId)
        if (resolved != model.financialProfileId) {
            transactionTemplateRepository.save(model.copy(financialProfileId = resolved))
            val template = existingTransactionTemplate(requireNotNull(model.id))
            template.versions.forEach { refresh(template, it, MaterializationOperation::FullRefresh) }
            enqueueProjectionChange(template.id)
        }
    }

    private suspend fun persistAndRefresh(
        template: TransactionTemplate,
        version: TransactionTemplateVersion,
        operation: (EffectiveTransactionTemplate) -> MaterializationOperation,
    ) {
        transactionTemplateVersionRepository.save(version.toModel())
        refresh(template, version, operation)
    }

    private suspend fun refresh(
        template: TransactionTemplate,
        version: TransactionTemplateVersion,
        operation: (EffectiveTransactionTemplate) -> MaterializationOperation,
    ) {
        val effective = EffectiveTransactionTemplate(template, version)
        transactionMaterializerService.materialize(operation(effective))
        upcomingTransactionCache?.refresh(effective)
    }

    private fun validateTimeline(index: Int, command: CreateTransactionTemplateWithVersionsCommand) {
        if (command.versions.isEmpty()) throw validation("Each transaction template requires at least one version", "templates[$index].versions")
        command.versions.zipWithNext().forEachIndexed { versionIndex, (previous, next) ->
            val previousStart = parseDate(previous.firstOccurrenceDate, "templates[$index].versions[$versionIndex].firstOccurrenceDate")
            val nextStart = parseDate(next.firstOccurrenceDate, "templates[$index].versions[${versionIndex + 1}].firstOccurrenceDate")
            if (!nextStart.isAfter(previousStart)) throw validation(
                "Transaction template versions must be ordered chronologically",
                "templates[$index].versions[${versionIndex + 1}].firstOccurrenceDate",
            )
        }
    }

    private suspend fun resolveFinancialProfileId(contractId: Long?, requestedProfileId: Long?): Long {
        val contractProfileId = contractId?.let { contractService.getById(it)?.financialProfileId ?: throw notFound("Contract", it) }
        return financialProfileService.resolveForAssignment(contractProfileId ?: requestedProfileId).id
    }

    private fun parseDate(value: String, field: String) = try { LocalDate.parse(value) } catch (_: DateTimeParseException) {
        throw validation("Transaction template date must use ISO-8601 format", field)
    }

    private fun requireAfter(later: LocalDate, earlier: LocalDate) {
        if (!later.isAfter(earlier)) throw validation("A transaction template version must start after the preceding version", "firstOccurrenceDate")
    }

    private fun validation(message: String, field: String) = ValidationException("validation_error", message, mapOf("field" to field))
    private fun notFound(type: String, id: Long) = NotFoundException("not_found", "$type not found", mapOf("id" to id))
    private suspend fun enqueueProjectionChange(id: Long) { projectionEventQueue?.enqueue(TransactionProjectionChangeEvent.TransactionTemplateChanged(id)) }
}

private fun CreateTransactionTemplateCommand.toBatchCommand() = CreateTransactionTemplateWithVersionsCommand(
    contractId, sourcePocketId, destinationPocketId, financialProfileId, partnerId, title, description,
    currencyCode, transactionType,
    listOf(
        CreateTransactionTemplateVersionCommand(
            amount, firstOccurrenceDate, finalOccurrenceDate, recurrenceType, skipCount,
            daysOfWeek, weeksOfMonth, daysOfMonth, monthsOfYear, maxRecurrenceCount,
        ),
    ),
)

private fun UpdateTransactionTemplateCommand.toRecurrencePattern() = RecurrencePattern(
    firstOccurrenceDate, finalOccurrenceDate, recurrenceType, skipCount,
    daysOfWeek, weeksOfMonth, daysOfMonth, monthsOfYear,
)
