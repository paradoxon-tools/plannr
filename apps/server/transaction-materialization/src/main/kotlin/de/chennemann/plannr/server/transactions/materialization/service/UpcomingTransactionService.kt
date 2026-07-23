package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.common.time.LocalDateProvider
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.transactions.materialization.api.dto.UpcomingTransactionItem
import de.chennemann.plannr.server.transactions.materialization.api.dto.UpcomingTransactionsResponse
import de.chennemann.plannr.server.transactions.templates.domain.TransactionTemplate
import de.chennemann.plannr.server.transactions.templates.service.TransactionTemplateService
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.Base64
import java.util.PriorityQueue
import org.springframework.stereotype.Service

@Service
class UpcomingTransactionService(
    private val transactionTemplateService: TransactionTemplateService,
    private val pocketService: PocketService,
    private val upcomingTransactionCache: UpcomingTransactionCache,
    private val upcomingOccurrenceCalculator: UpcomingOccurrenceCalculator,
    private val localDateProvider: LocalDateProvider,
) {
    suspend fun forAccount(
        accountId: Long,
        cursor: String?,
        limit: Int,
    ): UpcomingTransactionsResponse {
        val pocketIds = pocketService.list(accountId = accountId, archived = null).mapTo(mutableSetOf()) { it.id }
        return upcomingTransactions(
            cursor = cursor,
            limit = limit,
            templateFilter = { it.sourcePocketId in pocketIds || it.destinationPocketId in pocketIds },
        )
    }

    suspend fun forPocket(
        pocketId: Long,
        cursor: String?,
        limit: Int,
    ): UpcomingTransactionsResponse =
        upcomingTransactions(
            cursor = cursor,
            limit = limit,
            templateFilter = { it.sourcePocketId == pocketId || it.destinationPocketId == pocketId },
        )

    private suspend fun upcomingTransactions(
        cursor: String?,
        limit: Int,
        templateFilter: (TransactionTemplate) -> Boolean,
    ): UpcomingTransactionsResponse {
        validateLimit(limit)
        val decodedCursor = cursor?.let(::decodeCursor)
        val asOfDate = decodedCursor?.asOfDate ?: localDateProvider()
        val templates = transactionTemplateService.list(archived = false).filter(templateFilter)
        val queue = PriorityQueue<TemplateOccurrenceStream>(compareBy(TemplateOccurrenceStream::currentKey))

        templates.forEach { template ->
            initialStream(template, asOfDate, decodedCursor)?.let(queue::add)
        }

        val pageWithLookahead = mutableListOf<UpcomingTransactionItem>()
        while (queue.isNotEmpty() && pageWithLookahead.size < limit + 1) {
            val stream = queue.remove()
            pageWithLookahead += stream.currentItem()
            if (stream.advance()) {
                queue += stream
            }
        }

        val hasMore = pageWithLookahead.size > limit
        val page = pageWithLookahead.take(limit)
        val nextCursor = if (hasMore) {
            page.lastOrNull()?.let { encodeCursor(asOfDate, LocalDate.parse(it.occurrenceDate), it.transactionTemplateId) }
        } else {
            null
        }
        return UpcomingTransactionsResponse(
            asOfDate = asOfDate.toString(),
            transactions = page,
            nextCursor = nextCursor,
            hasMore = hasMore,
        )
    }

    private suspend fun initialStream(
        transactionTemplate: TransactionTemplate,
        asOfDate: LocalDate,
        cursor: UpcomingCursor?,
    ): TemplateOccurrenceStream? {
        val cursorKey = cursor?.let { OccurrenceKey(it.occurrenceDate, it.transactionTemplateId) }
        val cachedDates = if (asOfDate == localDateProvider()) {
            upcomingTransactionCache.getOrRefresh(transactionTemplate).map(LocalDate::parse)
        } else {
            emptyList()
        }
        val remainingCachedDates = cachedDates.filter {
            cursorKey == null || OccurrenceKey(it, transactionTemplate.id) > cursorKey
        }
        val initialDates = remainingCachedDates.ifEmpty {
            val afterExclusive = when {
                cursor == null -> asOfDate
                transactionTemplate.id > cursor.transactionTemplateId -> cursor.occurrenceDate.minusDays(1)
                else -> cursor.occurrenceDate
            }
            upcomingOccurrenceCalculator
                .nextExpansionAfter(transactionTemplate, afterExclusive)
                .filter { cursorKey == null || OccurrenceKey(it, transactionTemplate.id) > cursorKey }
        }
        return initialDates
            .takeIf(List<LocalDate>::isNotEmpty)
            ?.let { TemplateOccurrenceStream(transactionTemplate, it, upcomingOccurrenceCalculator) }
    }

    private fun validateLimit(limit: Int) {
        if (limit !in 1..MAX_PAGE_SIZE) {
            throw ValidationException(
                code = "validation_error",
                message = "Upcoming transaction page size must be between 1 and $MAX_PAGE_SIZE",
                details = mapOf("field" to "limit"),
            )
        }
    }

    private fun decodeCursor(encoded: String): UpcomingCursor =
        try {
            val decoded = String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8)
            val parts = decoded.split('|')
            require(parts.size == 3)
            UpcomingCursor(
                asOfDate = LocalDate.parse(parts[0]),
                occurrenceDate = LocalDate.parse(parts[1]),
                transactionTemplateId = parts[2].toLong(),
            ).also {
                require(it.transactionTemplateId > 0)
                require(it.occurrenceDate.isAfter(it.asOfDate))
            }
        } catch (exception: Exception) {
            throw ValidationException(
                code = "validation_error",
                message = "Upcoming transaction cursor is invalid",
                details = mapOf("field" to "cursor"),
            )
        }

    private fun encodeCursor(
        asOfDate: LocalDate,
        occurrenceDate: LocalDate,
        transactionTemplateId: Long,
    ): String =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("$asOfDate|$occurrenceDate|$transactionTemplateId".toByteArray(StandardCharsets.UTF_8))

    private data class UpcomingCursor(
        val asOfDate: LocalDate,
        val occurrenceDate: LocalDate,
        val transactionTemplateId: Long,
    )

    private data class OccurrenceKey(
        val occurrenceDate: LocalDate,
        val transactionTemplateId: Long,
    ) : Comparable<OccurrenceKey> {
        override fun compareTo(other: OccurrenceKey): Int =
            compareValuesBy(this, other, OccurrenceKey::occurrenceDate, OccurrenceKey::transactionTemplateId)
    }

    private class TemplateOccurrenceStream(
        private val transactionTemplate: TransactionTemplate,
        initialExpansion: List<LocalDate>,
        private val upcomingOccurrenceCalculator: UpcomingOccurrenceCalculator,
    ) {
        private var expansion = initialExpansion
        private var index = 0

        fun currentKey(): OccurrenceKey =
            OccurrenceKey(expansion[index], transactionTemplate.id)

        fun currentItem(): UpcomingTransactionItem =
            UpcomingTransactionItem(
                transactionTemplateId = transactionTemplate.id,
                occurrenceDate = expansion[index].toString(),
                sourcePocketId = transactionTemplate.sourcePocketId,
                destinationPocketId = transactionTemplate.destinationPocketId,
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
