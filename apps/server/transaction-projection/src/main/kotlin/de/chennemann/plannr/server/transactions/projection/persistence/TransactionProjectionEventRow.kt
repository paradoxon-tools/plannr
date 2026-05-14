package de.chennemann.plannr.server.transactions.projection.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("transaction_projection_events")
data class TransactionProjectionEventRow(
    @Id
    val id: Long?,
    @Column("event_type")
    val eventType: String,
    @Column("aggregate_id")
    val aggregateId: Long?,
    @Column("created_at")
    val createdAt: Long,
    @Column("processed_at")
    val processedAt: Long?,
)
