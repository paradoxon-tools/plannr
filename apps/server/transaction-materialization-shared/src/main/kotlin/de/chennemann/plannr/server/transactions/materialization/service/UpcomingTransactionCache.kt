package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate

interface UpcomingTransactionCache {
    suspend fun getOrRefresh(transactionTemplate: TransactionTemplate): List<String>
    suspend fun refresh(transactionTemplate: TransactionTemplate)
    fun invalidate(transactionTemplateId: Long)
}
