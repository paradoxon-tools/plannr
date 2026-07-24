package de.chennemann.plannr.server.transactions.projection.service

import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.transactions.projection.domain.TransactionProjectionEventRepository
import de.chennemann.plannr.server.transactions.projection.persistence.TransactionProjectionEventRow
import kotlinx.coroutines.flow.toList
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
internal class TransactionProjectionEvaluationJob(
    private val eventRepository: TransactionProjectionEventRepository,
    private val projectionRebuilder: TransactionProjectionRebuilder,
    private val timeProvider: TimeProvider,
) {
    @Scheduled(fixedDelayString = "\${plannr.projections.dirty-scope-delay-ms:30000}")
    suspend fun evaluatePendingEvents() {
        val pending = eventRepository.findPending(PENDING_BATCH_SIZE).toList()
        if (pending.isEmpty()) {
            return
        }

        projectionRebuilder.rebuildAll()
        eventRepository.markProcessed(
            ids = pending.map(TransactionProjectionEventRow::id).map(::requireNotNull),
            processedAt = timeProvider(),
        )
    }

    private companion object {
        const val PENDING_BATCH_SIZE = 250
    }
}
