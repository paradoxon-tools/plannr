package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionChangeEvent
import de.chennemann.plannr.server.transactions.projection.service.TransactionProjectionEventQueue
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
internal class DailyTransactionMaterializationJob(
    private val transactionTemplateService: TransactionTemplateService,
    private val transactionMaterializerService: TransactionMaterializerService,
    private val upcomingTransactionCache: UpcomingTransactionCache,
    private val projectionEventQueue: TransactionProjectionEventQueue,
) {
    @Scheduled(
        cron = "\${plannr.transactions.materialization.daily-cron:0 5 0 * * *}",
    )
    suspend fun materializeTransactionsThroughToday() {
        val activeTemplates = transactionTemplateService.list(archived = false)
        activeTemplates.forEach { transactionTemplate ->
            transactionMaterializerService.materialize(MaterializationOperation.EndDateChange(transactionTemplate))
            upcomingTransactionCache.refresh(transactionTemplate)
        }
        projectionEventQueue.enqueue(TransactionProjectionChangeEvent.FullRebuild)
    }
}
