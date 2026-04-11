package de.chennemann.plannr.server.query.transactions.usecases

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.query.accounts.usecases.GetAccountQuery
import de.chennemann.plannr.server.query.pockets.usecases.GetPocketQuery
import de.chennemann.plannr.server.query.transactions.domain.AccountFutureTransactionFeedItem
import de.chennemann.plannr.server.query.transactions.domain.AccountFutureTransactionFeedRepository
import de.chennemann.plannr.server.query.transactions.domain.PocketFutureTransactionFeedItem
import de.chennemann.plannr.server.query.transactions.domain.PocketFutureTransactionFeedRepository
import org.springframework.stereotype.Component

interface ListAccountFutureTransactionFeed {
    suspend operator fun invoke(accountId: String, fromDate: String? = null, toDate: String? = null, after: Long? = null, limit: Int = DEFAULT_LIMIT): Page
    data class Page(val items: List<AccountFutureTransactionFeedItem>, val nextAfter: Long?)
    companion object { const val DEFAULT_LIMIT = 50; const val MAX_LIMIT = 200 }
}

interface ListPocketFutureTransactionFeed {
    suspend operator fun invoke(pocketId: String, fromDate: String? = null, toDate: String? = null, after: Long? = null, limit: Int = DEFAULT_LIMIT): Page
    data class Page(val items: List<PocketFutureTransactionFeedItem>, val nextAfter: Long?)
    companion object { const val DEFAULT_LIMIT = 50; const val MAX_LIMIT = 200 }
}

interface ListContractHistoricalTransactionFeed {
    suspend operator fun invoke(contractId: String, before: Long? = null, limit: Int = DEFAULT_LIMIT): ListPocketTransactionFeed.Page
    companion object { const val DEFAULT_LIMIT = 50 }
}

interface ListContractFutureTransactionFeed {
    suspend operator fun invoke(contractId: String, fromDate: String? = null, toDate: String? = null, after: Long? = null, limit: Int = DEFAULT_LIMIT): ListPocketFutureTransactionFeed.Page
    companion object { const val DEFAULT_LIMIT = 50 }
}

@Component
internal class ListAccountFutureTransactionFeedUseCase(
    private val getAccountQuery: GetAccountQuery,
    private val repository: AccountFutureTransactionFeedRepository,
) : ListAccountFutureTransactionFeed {
    override suspend fun invoke(accountId: String, fromDate: String?, toDate: String?, after: Long?, limit: Int): ListAccountFutureTransactionFeed.Page {
        getAccountQuery(accountId.trim())
        val normalizedLimit = normalizeLimit(limit, ListAccountFutureTransactionFeed.MAX_LIMIT)
        val items = repository.findPage(accountId.trim(), fromDate, toDate, after, normalizedLimit)
        return ListAccountFutureTransactionFeed.Page(items, items.lastOrNull()?.futurePosition)
    }
}

@Component
internal class ListPocketFutureTransactionFeedUseCase(
    private val getPocketQuery: GetPocketQuery,
    private val repository: PocketFutureTransactionFeedRepository,
) : ListPocketFutureTransactionFeed {
    override suspend fun invoke(pocketId: String, fromDate: String?, toDate: String?, after: Long?, limit: Int): ListPocketFutureTransactionFeed.Page {
        getPocketQuery(pocketId.trim())
        val normalizedLimit = normalizeLimit(limit, ListPocketFutureTransactionFeed.MAX_LIMIT)
        val items = repository.findPageByPocketId(pocketId.trim(), fromDate, toDate, after, normalizedLimit)
        return ListPocketFutureTransactionFeed.Page(items, items.lastOrNull()?.futurePosition)
    }
}

@Component
internal class ListContractHistoricalTransactionFeedUseCase(
    private val contractRepository: ContractRepository,
    private val pocketFeedRepository: de.chennemann.plannr.server.query.transactions.persistence.R2dbcPocketTransactionFeedRepository,
) : ListContractHistoricalTransactionFeed {
    override suspend fun invoke(contractId: String, before: Long?, limit: Int): ListPocketTransactionFeed.Page {
        contractRepository.findById(contractId.trim()) ?: throw de.chennemann.plannr.server.common.error.NotFoundException("not_found", "Contract not found", mapOf("id" to contractId.trim()))
        val items = pocketFeedRepository.findPageByContractId(contractId.trim(), before, normalizeLimit(limit, 200))
        return ListPocketTransactionFeed.Page(items, items.lastOrNull()?.historyPosition)
    }
}

@Component
internal class ListContractFutureTransactionFeedUseCase(
    private val contractRepository: ContractRepository,
    private val repository: PocketFutureTransactionFeedRepository,
) : ListContractFutureTransactionFeed {
    override suspend fun invoke(contractId: String, fromDate: String?, toDate: String?, after: Long?, limit: Int): ListPocketFutureTransactionFeed.Page {
        contractRepository.findById(contractId.trim()) ?: throw de.chennemann.plannr.server.common.error.NotFoundException("not_found", "Contract not found", mapOf("id" to contractId.trim()))
        val items = repository.findPageByContractId(contractId.trim(), fromDate, toDate, after, normalizeLimit(limit, 200))
        return ListPocketFutureTransactionFeed.Page(items, items.lastOrNull()?.futurePosition)
    }
}

private fun normalizeLimit(limit: Int, max: Int): Int {
    if (limit !in 1..max) throw ValidationException("validation_error", "Query limit must be between 1 and $max", mapOf("limit" to limit))
    return limit
}
