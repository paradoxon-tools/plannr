package de.chennemann.plannr.server.transactions.templates.domain

import de.chennemann.plannr.server.transactions.templates.persistence.TransactionTemplateModel
import de.chennemann.plannr.server.transactions.templates.persistence.toDomain
import de.chennemann.plannr.server.transactions.templates.persistence.toModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TransactionTemplateRepository : CoroutineCrudRepository<TransactionTemplateModel, Long> {
    fun findAllByIsArchivedOrderByCreatedAtAscIdAsc(isArchived: Boolean): Flow<TransactionTemplateModel>
    fun findAllBySourcePocketIdAndIsArchivedOrDestinationPocketIdAndIsArchivedOrderByCreatedAtAscIdAsc(
        sourcePocketId: Long,
        sourceIsArchived: Boolean,
        destinationPocketId: Long,
        destinationIsArchived: Boolean,
    ): Flow<TransactionTemplateModel>
}

suspend fun TransactionTemplateRepository.save(transactionTemplate: TransactionTemplate): TransactionTemplate =
    save(transactionTemplate.toModel()).toDomain()
