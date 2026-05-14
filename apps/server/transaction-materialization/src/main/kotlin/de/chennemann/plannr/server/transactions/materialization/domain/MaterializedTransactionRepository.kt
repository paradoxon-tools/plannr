package de.chennemann.plannr.server.transactions.materialization.domain

import de.chennemann.plannr.server.transactions.materialization.persistence.MaterializedTransactionModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface MaterializedTransactionRepository : CoroutineCrudRepository<MaterializedTransactionModel, Long> {
    fun findAllByTransactionTemplateIdOrderByTransactionDateAscIdAsc(transactionTemplateId: Long): Flow<MaterializedTransactionModel>

    suspend fun findByTransactionTemplateIdAndTransactionDate(transactionTemplateId: Long, transactionDate: String): MaterializedTransactionModel?

    suspend fun deleteAllByTransactionTemplateId(transactionTemplateId: Long)

    suspend fun deleteAllByTransactionTemplateIdAndTransactionDateNotIn(transactionTemplateId: Long, transactionDates: Collection<String>)
}
