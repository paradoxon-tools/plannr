package de.chennemann.plannr.server.transactions.usecases

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.pockets.usecases.GetPocketQuery
import de.chennemann.plannr.server.transactions.domain.PocketTransactionFeedItem
import de.chennemann.plannr.server.transactions.domain.PocketTransactionFeedRepository
import de.chennemann.plannr.server.transactions.usecases.ListPocketTransactionFeed.Page
import org.springframework.stereotype.Component

interface ListPocketTransactionFeed {
    suspend operator fun invoke(pocketId: String, before: Long? = null, limit: Int = DEFAULT_LIMIT): Page

    data class Page(
        val items: List<PocketTransactionFeedItem>,
        val nextBefore: Long?,
    )

    companion object {
        const val DEFAULT_LIMIT = 50
        const val MAX_LIMIT = 200
    }
}

@Component
internal class ListPocketTransactionFeedUseCase(
    private val getPocketQuery: GetPocketQuery,
    private val pocketTransactionFeedRepository: PocketTransactionFeedRepository,
) : ListPocketTransactionFeed {
    override suspend fun invoke(pocketId: String, before: Long?, limit: Int): Page {
        val normalizedLimit = normalizeLimit(limit)
        val normalizedPocketId = pocketId.trim()
        getPocketQuery(normalizedPocketId)
        val items = pocketTransactionFeedRepository.findPage(normalizedPocketId, before, normalizedLimit)
        return Page(
            items = items,
            nextBefore = items.lastOrNull()?.historyPosition,
        )
    }

    private fun normalizeLimit(limit: Int): Int {
        if (limit !in 1..ListPocketTransactionFeed.MAX_LIMIT) {
            throw ValidationException(
                code = "validation_error",
                message = "Query limit must be between 1 and ${ListPocketTransactionFeed.MAX_LIMIT}",
                details = mapOf("limit" to limit),
            )
        }
        return limit
    }
}
