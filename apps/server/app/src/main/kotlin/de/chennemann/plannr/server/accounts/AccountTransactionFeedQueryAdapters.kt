package de.chennemann.plannr.server.accounts

import de.chennemann.plannr.server.accounts.api.AccountFutureTransactionFeedQuery
import de.chennemann.plannr.server.accounts.api.AccountTransactionFeedQuery
import de.chennemann.plannr.server.transactions.api.toResponse
import de.chennemann.plannr.server.transactions.api.dto.AccountFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.AccountTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.service.TransactionFeedService
import org.springframework.stereotype.Component

@Component
internal class ServiceAccountTransactionFeedQuery(
    private val transactionFeedService: TransactionFeedService,
) : AccountTransactionFeedQuery {
    override suspend fun list(
        accountId: String,
        limit: Int,
        before: Long?,
    ): AccountTransactionFeedPageResponse =
        transactionFeedService.listAccountTransactions(accountId, before, limit).toResponse()
}

@Component
internal class ServiceAccountFutureTransactionFeedQuery(
    private val transactionFeedService: TransactionFeedService,
) : AccountFutureTransactionFeedQuery {
    override suspend fun list(
        accountId: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): AccountFutureTransactionFeedPageResponse =
        transactionFeedService.listAccountFutureTransactions(accountId, fromDate, toDate, after, limit).toResponse()
}

