package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DailyTransactionMaterializationJob(
    private val transactionTemplateService: TransactionTemplateService,
    private val transactionMaterializerService: TransactionMaterializerService,
) {
    @Scheduled(cron = "\${plannr.transactions.materialization.daily-cron:0 15 2 * * *}")
    suspend fun materializeFutureTransactions() {
        transactionTemplateService.list(archived = false)
            .forEach { transactionTemplate ->
                transactionMaterializerService.materialize(MaterializationOperation.EndDateChange(transactionTemplate))
            }
    }
}
