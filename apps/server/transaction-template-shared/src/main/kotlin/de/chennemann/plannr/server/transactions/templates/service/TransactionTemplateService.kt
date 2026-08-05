package de.chennemann.plannr.server.transactions.templates.service

import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateVersionCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.CreateTransactionTemplateWithVersionsCommand
import de.chennemann.plannr.server.transactions.templates.api.dto.UpdateTransactionTemplateCommand
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate

interface TransactionTemplateService {
    suspend fun create(command: CreateTransactionTemplateCommand): TransactionTemplate
    suspend fun createBatch(commands: List<CreateTransactionTemplateWithVersionsCommand>): List<TransactionTemplate>
    suspend fun createVersion(transactionTemplateId: Long, command: CreateTransactionTemplateVersionCommand): TransactionTemplate
    suspend fun update(command: UpdateTransactionTemplateCommand): TransactionTemplate
    suspend fun archive(id: Long): TransactionTemplate
    suspend fun unarchive(id: Long): TransactionTemplate
    suspend fun archiveForPocket(pocketId: Long)
    suspend fun unarchiveForPocket(pocketId: Long)
    suspend fun refreshFinancialProfilesForPocket(pocketId: Long)
    suspend fun refreshFinancialProfilesForContract(contractId: Long) = Unit
    suspend fun delete(id: Long)
    suspend fun deleteVersion(transactionTemplateId: Long, versionId: Long): TransactionTemplate?
    suspend fun list(archived: Boolean? = null): List<TransactionTemplate>
    suspend fun getById(id: Long): TransactionTemplate?
}
