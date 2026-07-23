package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.common.time.LocalDateProvider
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import org.springframework.stereotype.Component

@Component
class InMemoryUpcomingTransactionCache(
    private val localDateProvider: LocalDateProvider,
    private val upcomingOccurrenceCalculator: UpcomingOccurrenceCalculator,
) : UpcomingTransactionCache {
    private val entries = ConcurrentHashMap<Long, CacheEntry>()

    override suspend fun getOrRefresh(transactionTemplate: TransactionTemplate): List<String> {
        val asOfDate = localDateProvider()
        val cached = entries[transactionTemplate.id]
        if (
            cached != null &&
            cached.asOfDate == asOfDate &&
            cached.transactionTemplate == transactionTemplate
        ) {
            return cached.occurrenceDates
        }
        return calculateAndStore(transactionTemplate, asOfDate)
    }

    override suspend fun refresh(transactionTemplate: TransactionTemplate) {
        calculateAndStore(transactionTemplate, localDateProvider())
    }

    override fun invalidate(transactionTemplateId: Long) {
        entries.remove(transactionTemplateId)
    }

    private fun calculateAndStore(
        transactionTemplate: TransactionTemplate,
        asOfDate: LocalDate,
    ): List<String> {
        if (transactionTemplate.isArchived) {
            entries.remove(transactionTemplate.id)
            return emptyList()
        }
        val occurrenceDates = upcomingOccurrenceCalculator
            .nextExpansionAfter(transactionTemplate, asOfDate)
            .map(LocalDate::toString)
        entries[transactionTemplate.id] = CacheEntry(
            transactionTemplate = transactionTemplate,
            asOfDate = asOfDate,
            occurrenceDates = occurrenceDates,
        )
        return occurrenceDates
    }

    private data class CacheEntry(
        val transactionTemplate: TransactionTemplate,
        val asOfDate: LocalDate,
        val occurrenceDates: List<String>,
    )
}
