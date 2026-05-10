package de.chennemann.plannr.server.transactions.recurring.domain

import de.chennemann.plannr.server.transactions.recurring.persistence.RecurringTransactionModel
import de.chennemann.plannr.server.transactions.recurring.persistence.toDomain
import de.chennemann.plannr.server.transactions.recurring.persistence.toModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface RecurringTransactionRepository : CoroutineCrudRepository<RecurringTransactionModel, Long> {
    fun findAllByIsArchivedOrderByCreatedAtAscIdAsc(isArchived: Boolean): Flow<RecurringTransactionModel>
    fun findAllBySourcePocketIdAndIsArchivedOrDestinationPocketIdAndIsArchivedOrderByCreatedAtAscIdAsc(
        sourcePocketId: Long,
        sourceIsArchived: Boolean,
        destinationPocketId: Long,
        destinationIsArchived: Boolean,
    ): Flow<RecurringTransactionModel>
}

suspend fun RecurringTransactionRepository.save(recurringTransaction: RecurringTransaction): RecurringTransaction =
    save(recurringTransaction.toModel()).toDomain()
