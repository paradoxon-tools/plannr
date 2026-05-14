package de.chennemann.plannr.server.transactions.projection.persistence

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TransactionProjectionEventRepository : CoroutineCrudRepository<TransactionProjectionEventRow, Long> {
    @Query(
        """
        SELECT id, event_type, aggregate_id, created_at, processed_at
        FROM transaction_projection_events
        WHERE processed_at IS NULL
        ORDER BY id ASC
        LIMIT :limit
        """,
    )
    fun findPending(limit: Int): Flow<TransactionProjectionEventRow>

    @Modifying
    @Query(
        """
        UPDATE transaction_projection_events
        SET processed_at = :processedAt
        WHERE id IN (:ids)
        """,
    )
    suspend fun markProcessed(ids: Collection<Long>, processedAt: Long): Int
}
