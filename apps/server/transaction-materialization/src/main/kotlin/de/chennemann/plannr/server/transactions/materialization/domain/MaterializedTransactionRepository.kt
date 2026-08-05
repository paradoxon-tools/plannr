package de.chennemann.plannr.server.transactions.materialization.domain

import de.chennemann.plannr.server.transactions.materialization.persistence.MaterializedTransactionModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface MaterializedTransactionRepository : CoroutineCrudRepository<MaterializedTransactionModel, Long> {
    fun findAllByTransactionTemplateVersionIdOrderByTransactionDateAscIdAsc(transactionTemplateVersionId: Long): Flow<MaterializedTransactionModel>

    suspend fun findByTransactionTemplateVersionIdAndTransactionDate(transactionTemplateVersionId: Long, transactionDate: String): MaterializedTransactionModel?

    suspend fun deleteAllByTransactionTemplateVersionId(transactionTemplateVersionId: Long)

    suspend fun deleteAllByTransactionTemplateVersionIdAndTransactionDateNotIn(transactionTemplateVersionId: Long, transactionDates: Collection<String>)
}
