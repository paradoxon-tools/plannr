package de.chennemann.plannr.server.transactions.service

import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.contracts.domain.ContractRepository
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.transactions.domain.AccountFutureTransactionFeedItem
import de.chennemann.plannr.server.transactions.domain.AccountFutureTransactionFeedRepository
import de.chennemann.plannr.server.transactions.domain.AccountTransactionFeedItem
import de.chennemann.plannr.server.transactions.domain.AccountTransactionFeedRepository
import de.chennemann.plannr.server.transactions.domain.PocketFutureTransactionFeedItem
import de.chennemann.plannr.server.transactions.domain.PocketFutureTransactionFeedRepository
import de.chennemann.plannr.server.transactions.domain.PocketTransactionFeedItem
import de.chennemann.plannr.server.transactions.domain.PocketTransactionFeedRepository
import de.chennemann.plannr.server.transactions.persistence.R2dbcPocketTransactionFeedRepository
import org.springframework.stereotype.Service

@Service
class TransactionFeedService(
    private val accountService: AccountService,
    private val accountTransactionFeedRepository: AccountTransactionFeedRepository,
    private val accountFutureTransactionFeedRepository: AccountFutureTransactionFeedRepository,
    private val pocketService: PocketService,
    private val pocketTransactionFeedRepository: PocketTransactionFeedRepository,
    private val pocketFutureTransactionFeedRepository: PocketFutureTransactionFeedRepository,
    private val contractRepository: ContractRepository,
    private val contractHistoricalFeedRepository: R2dbcPocketTransactionFeedRepository,
) {
    suspend fun listAccountTransactions(accountId: String, before: Long? = null, limit: Int = DEFAULT_LIMIT): AccountTransactionFeedPage {
        val normalizedLimit = normalizeLimit(limit, MAX_LIMIT)
        val normalizedAccountId = accountId.trim()
        accountService.getQuery(normalizedAccountId)
        val items = accountTransactionFeedRepository.findPage(normalizedAccountId, before, normalizedLimit)
        return AccountTransactionFeedPage(items, items.lastOrNull()?.historyPosition)
    }

    suspend fun listPocketTransactions(pocketId: String, before: Long? = null, limit: Int = DEFAULT_LIMIT): PocketTransactionFeedPage {
        val normalizedLimit = normalizeLimit(limit, MAX_LIMIT)
        val normalizedPocketId = pocketId.trim()
        pocketService.getQuery(normalizedPocketId)
        val items = pocketTransactionFeedRepository.findPage(normalizedPocketId, before, normalizedLimit)
        return PocketTransactionFeedPage(items, items.lastOrNull()?.historyPosition)
    }

    suspend fun listAccountFutureTransactions(
        accountId: String,
        fromDate: String? = null,
        toDate: String? = null,
        after: Long? = null,
        limit: Int = DEFAULT_LIMIT,
    ): AccountFutureTransactionFeedPage {
        accountService.getQuery(accountId.trim())
        val normalizedLimit = normalizeLimit(limit, MAX_LIMIT)
        val items = accountFutureTransactionFeedRepository.findPage(accountId.trim(), fromDate, toDate, after, normalizedLimit)
        return AccountFutureTransactionFeedPage(items, items.lastOrNull()?.futurePosition)
    }

    suspend fun listPocketFutureTransactions(
        pocketId: String,
        fromDate: String? = null,
        toDate: String? = null,
        after: Long? = null,
        limit: Int = DEFAULT_LIMIT,
    ): PocketFutureTransactionFeedPage {
        pocketService.getQuery(pocketId.trim())
        val normalizedLimit = normalizeLimit(limit, MAX_LIMIT)
        val items = pocketFutureTransactionFeedRepository.findPageByPocketId(pocketId.trim(), fromDate, toDate, after, normalizedLimit)
        return PocketFutureTransactionFeedPage(items, items.lastOrNull()?.futurePosition)
    }

    suspend fun listContractHistoricalTransactions(contractId: String, before: Long? = null, limit: Int = DEFAULT_LIMIT): PocketTransactionFeedPage {
        val normalizedContractId = contractId.trim()
        contractRepository.findById(normalizedContractId)
            ?: throw NotFoundException("not_found", "Contract not found", mapOf("id" to normalizedContractId))
        val items = contractHistoricalFeedRepository.findPageByContractId(normalizedContractId, before, normalizeLimit(limit, MAX_LIMIT))
        return PocketTransactionFeedPage(items, items.lastOrNull()?.historyPosition)
    }

    suspend fun listContractFutureTransactions(
        contractId: String,
        fromDate: String? = null,
        toDate: String? = null,
        after: Long? = null,
        limit: Int = DEFAULT_LIMIT,
    ): PocketFutureTransactionFeedPage {
        val normalizedContractId = contractId.trim()
        contractRepository.findById(normalizedContractId)
            ?: throw NotFoundException("not_found", "Contract not found", mapOf("id" to normalizedContractId))
        val items = pocketFutureTransactionFeedRepository.findPageByContractId(normalizedContractId, fromDate, toDate, after, normalizeLimit(limit, MAX_LIMIT))
        return PocketFutureTransactionFeedPage(items, items.lastOrNull()?.futurePosition)
    }

    data class AccountTransactionFeedPage(
        val items: List<AccountTransactionFeedItem>,
        val nextBefore: Long?,
    )

    data class PocketTransactionFeedPage(
        val items: List<PocketTransactionFeedItem>,
        val nextBefore: Long?,
    )

    data class AccountFutureTransactionFeedPage(
        val items: List<AccountFutureTransactionFeedItem>,
        val nextAfter: Long?,
    )

    data class PocketFutureTransactionFeedPage(
        val items: List<PocketFutureTransactionFeedItem>,
        val nextAfter: Long?,
    )

    companion object {
        const val DEFAULT_LIMIT = 50
        const val MAX_LIMIT = 200
    }
}

private fun normalizeLimit(limit: Int, max: Int): Int {
    if (limit !in 1..max) throw ValidationException("validation_error", "Query limit must be between 1 and $max", mapOf("limit" to limit))
    return limit
}
