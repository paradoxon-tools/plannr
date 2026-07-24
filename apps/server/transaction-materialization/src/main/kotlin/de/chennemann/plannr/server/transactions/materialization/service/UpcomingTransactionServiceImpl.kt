package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.common.time.LocalDateProvider
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.transactions.materialization.api.dto.UpcomingTransactionItem
import de.chennemann.plannr.server.transactions.materialization.api.dto.UpcomingTransactionsResponse
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService
import java.time.LocalDate
import java.util.PriorityQueue
import org.springframework.stereotype.Component

@Component
internal class UpcomingTransactionServiceImpl(
    private val transactionTemplateService: TransactionTemplateService,
    private val pocketService: PocketService,
    private val upcomingTransactionCache: UpcomingTransactionCache,
    private val upcomingOccurrenceCalculator: UpcomingOccurrenceCalculator,
    private val localDateProvider: LocalDateProvider,
) : UpcomingTransactionService {
    override suspend fun getForAccount(
        accountId: Long,
        after: LocalDate?,
        count: Int,
    ): UpcomingTransactionsResponse {
        val pocketIds = pocketService.list(accountId = accountId, archived = null).mapTo(mutableSetOf()) { it.id }
        return upcomingTransactions(
            after = after,
            count = count,
            templateFilter = { it.sourcePocketId in pocketIds || it.destinationPocketId in pocketIds },
        )
    }

    override suspend fun getForPocket(
        pocketId: Long,
        after: LocalDate?,
        count: Int,
    ): UpcomingTransactionsResponse =
        upcomingTransactions(
            after = after,
            count = count,
            templateFilter = { it.sourcePocketId == pocketId || it.destinationPocketId == pocketId },
        )

    private suspend fun upcomingTransactions(
        after: LocalDate?,
        count: Int,
        templateFilter: (TransactionTemplate) -> Boolean,
    ): UpcomingTransactionsResponse {
        validateCount(count)
        val afterDate = after ?: localDateProvider()
        val templates = transactionTemplateService.list(archived = false).filter(templateFilter)
        val queue = PriorityQueue<TemplateOccurrenceStream>(
            compareBy(TemplateOccurrenceStream::currentDate)
                .thenBy(TemplateOccurrenceStream::templateId),
        )

        templates.forEach { template ->
            initialStream(template, afterDate)?.let(queue::add)
        }

        val transactions = mutableListOf<UpcomingTransactionItem>()
        var lastReturnedDate: LocalDate? = null
        while (
            queue.isNotEmpty() &&
            (
                transactions.size < count ||
                    queue.peek().currentDate() == lastReturnedDate
                )
        ) {
            val stream = queue.remove()
            lastReturnedDate = stream.currentDate()
            transactions += stream.currentItem()
            if (stream.advance()) {
                queue += stream
            }
        }

        return UpcomingTransactionsResponse(
            afterDate = afterDate.toString(),
            transactions = transactions,
            hasMore = queue.isNotEmpty(),
        )
    }

    private suspend fun initialStream(
        transactionTemplate: TransactionTemplate,
        afterDate: LocalDate,
    ): TemplateOccurrenceStream? {
        val cachedDates = if (afterDate == localDateProvider()) {
            upcomingTransactionCache.getOrRefresh(transactionTemplate).map(LocalDate::parse)
        } else {
            emptyList()
        }
        val initialDates = cachedDates.filter { it.isAfter(afterDate) }.ifEmpty {
            upcomingOccurrenceCalculator
                .nextExpansionAfter(transactionTemplate, afterDate)
        }
        return initialDates
            .takeIf(List<LocalDate>::isNotEmpty)
            ?.let { TemplateOccurrenceStream(transactionTemplate, it, upcomingOccurrenceCalculator) }
    }

    private fun validateCount(count: Int) {
        if (count !in 1..MAX_PAGE_SIZE) {
            throw ValidationException(
                code = "validation_error",
                message = "Upcoming transaction count must be between 1 and $MAX_PAGE_SIZE",
                details = mapOf("field" to "count"),
            )
        }
    }

    private class TemplateOccurrenceStream(
        private val transactionTemplate: TransactionTemplate,
        initialExpansion: List<LocalDate>,
        private val upcomingOccurrenceCalculator: UpcomingOccurrenceCalculator,
    ) {
        private var expansion = initialExpansion
        private var index = 0

        fun currentDate(): LocalDate = expansion[index]

        fun templateId(): Long = transactionTemplate.id

        fun currentItem(): UpcomingTransactionItem =
            UpcomingTransactionItem(
                transactionTemplateId = transactionTemplate.id,
                occurrenceDate = expansion[index].toString(),
                sourcePocketId = transactionTemplate.sourcePocketId,
                destinationPocketId = transactionTemplate.destinationPocketId,
                financialProfileId = transactionTemplate.financialProfileId,
                partnerId = transactionTemplate.partnerId,
                type = transactionTemplate.transactionType,
                title = transactionTemplate.title,
                description = transactionTemplate.description,
                amount = transactionTemplate.amount,
                currencyCode = transactionTemplate.currencyCode,
            )

        fun advance(): Boolean {
            index += 1
            if (index < expansion.size) {
                return true
            }
            expansion = upcomingOccurrenceCalculator.nextExpansionAfter(
                transactionTemplate = transactionTemplate,
                afterExclusive = expansion.last(),
            )
            index = 0
            return expansion.isNotEmpty()
        }
    }

    private companion object {
        const val MAX_PAGE_SIZE = 100
    }
}
