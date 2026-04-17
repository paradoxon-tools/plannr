package de.chennemann.plannr.server.transactions.usecases

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.accounts.usecases.GetAccountQuery
import de.chennemann.plannr.server.transactions.domain.AccountTransactionFeedItem
import de.chennemann.plannr.server.transactions.domain.AccountTransactionFeedRepository
import de.chennemann.plannr.server.transactions.usecases.ListAccountTransactionFeed.Page
import org.springframework.stereotype.Component

interface ListAccountTransactionFeed {
    suspend operator fun invoke(accountId: String, before: Long? = null, limit: Int = DEFAULT_LIMIT): Page

    data class Page(
        val items: List<AccountTransactionFeedItem>,
        val nextBefore: Long?,
    )

    companion object {
        const val DEFAULT_LIMIT = 50
        const val MAX_LIMIT = 200
    }
}

@Component
internal class ListAccountTransactionFeedUseCase(
    private val getAccountQuery: GetAccountQuery,
    private val accountTransactionFeedRepository: AccountTransactionFeedRepository,
) : ListAccountTransactionFeed {
    override suspend fun invoke(accountId: String, before: Long?, limit: Int): Page {
        val normalizedLimit = normalizeLimit(limit)
        val normalizedAccountId = accountId.trim()
        getAccountQuery(normalizedAccountId)
        val items = accountTransactionFeedRepository.findPage(normalizedAccountId, before, normalizedLimit)
        return Page(
            items = items,
            nextBefore = items.lastOrNull()?.historyPosition,
        )
    }

    private fun normalizeLimit(limit: Int): Int {
        if (limit !in 1..ListAccountTransactionFeed.MAX_LIMIT) {
            throw ValidationException(
                code = "validation_error",
                message = "Query limit must be between 1 and ${ListAccountTransactionFeed.MAX_LIMIT}",
                details = mapOf("limit" to limit),
            )
        }
        return limit
    }
}
