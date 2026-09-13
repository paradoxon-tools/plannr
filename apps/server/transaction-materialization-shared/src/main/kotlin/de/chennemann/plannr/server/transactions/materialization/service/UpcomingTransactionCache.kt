package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.transactions.templates.domain.EffectiveTransactionTemplate

interface UpcomingTransactionCache {
    suspend fun getOrRefresh(transactionTemplate: EffectiveTransactionTemplate): List<String>
    suspend fun refresh(transactionTemplate: EffectiveTransactionTemplate)
    fun invalidate(transactionTemplateId: Long)
}
