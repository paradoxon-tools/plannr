package de.chennemann.plannr.server.transactions.templates.domain

import de.chennemann.plannr.server.transactions.templates.persistence.TransactionTemplateModel
import de.chennemann.plannr.server.transactions.templates.persistence.TransactionTemplateVersionModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TransactionTemplateRepository : CoroutineCrudRepository<TransactionTemplateModel, Long> {
    fun findAllByIsArchivedOrderByCreatedAtAscIdAsc(isArchived: Boolean): Flow<TransactionTemplateModel>
}

interface TransactionTemplateVersionRepository : CoroutineCrudRepository<TransactionTemplateVersionModel, Long> {
    fun findAllByTransactionTemplateIdOrderByValidFromAscIdAsc(transactionTemplateId: Long): Flow<TransactionTemplateVersionModel>
    suspend fun deleteAllByTransactionTemplateId(transactionTemplateId: Long)
}
