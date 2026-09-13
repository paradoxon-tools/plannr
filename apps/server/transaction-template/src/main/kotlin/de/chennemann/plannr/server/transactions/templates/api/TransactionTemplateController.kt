package de.chennemann.plannr.server.transactions.templates.api

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplatesCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateVersionCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.api.dto.UpdateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionTemplateController(
    private val transactionTemplateService: TransactionTemplateService,
) : TransactionTemplateApi {
    override suspend fun create(command: CreateTransactionTemplateCommand): TransactionTemplate =
        transactionTemplateService.create(command).toDTO()

    override suspend fun createBatch(command: CreateTransactionTemplatesCommand): List<TransactionTemplate> =
        transactionTemplateService.createBatch(command.templates)
            .map { it.toDTO() }

    override suspend fun createVersion(id: Long, command: CreateTransactionTemplateVersionCommand): TransactionTemplate =
        transactionTemplateService.createVersion(id, command).toDTO()

    override suspend fun update(command: UpdateTransactionTemplateCommand): TransactionTemplate =
        transactionTemplateService.update(command).toDTO()

    override suspend fun archive(id: Long): TransactionTemplate =
        transactionTemplateService.archive(id).toDTO()

    override suspend fun unarchive(id: Long): TransactionTemplate =
        transactionTemplateService.unarchive(id).toDTO()

    override suspend fun delete(id: Long) =
        transactionTemplateService.delete(id)

    override suspend fun deleteVersion(id: Long, versionId: Long): TransactionTemplate? =
        transactionTemplateService.deleteVersion(id, versionId)?.toDTO()

    override suspend fun list(archived: Boolean?): List<TransactionTemplate> =
        transactionTemplateService.list(archived).map { it.toDTO() }

    override suspend fun getById(id: Long): TransactionTemplate =
        transactionTemplateService.getById(id)?.toDTO()
            ?: throw NotFoundException("not_found", "Transaction template not found", mapOf("id" to id))
}
