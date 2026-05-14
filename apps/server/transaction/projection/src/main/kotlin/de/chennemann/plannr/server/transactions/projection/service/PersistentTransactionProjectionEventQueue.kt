package de.chennemann.plannr.server.transactions.projection.service

import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.transactions.projection.persistence.TransactionProjectionEventRepository
import de.chennemann.plannr.server.transactions.projection.persistence.TransactionProjectionEventRow
import org.springframework.stereotype.Component

@Component
class PersistentTransactionProjectionEventQueue(
    private val repository: TransactionProjectionEventRepository,
    private val timeProvider: TimeProvider,
) : TransactionProjectionEventQueue {
    override suspend fun enqueue(event: TransactionProjectionChangeEvent) {
        repository.save(
            TransactionProjectionEventRow(
                id = null,
                eventType = event.eventType,
                aggregateId = event.aggregateId,
                createdAt = timeProvider(),
                processedAt = null,
            ),
        )
    }
}
