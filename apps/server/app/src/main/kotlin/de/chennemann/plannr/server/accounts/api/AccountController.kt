package de.chennemann.plannr.server.accounts.api

import de.chennemann.plannr.server.accounts.api.dto.AccountQueryResponse
import de.chennemann.plannr.server.accounts.api.dto.AccountResponse
import de.chennemann.plannr.server.accounts.api.dto.CreateAccountRequest
import de.chennemann.plannr.server.accounts.api.dto.UpdateAccountRequest
import de.chennemann.plannr.server.accounts.service.AccountService
import de.chennemann.plannr.server.transactions.api.dto.AccountFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.AccountTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.toResponse
import de.chennemann.plannr.server.transactions.usecases.ListAccountFutureTransactionFeed
import de.chennemann.plannr.server.transactions.usecases.ListAccountTransactionFeed
import org.springframework.web.bind.annotation.RestController

@RestController
class AccountController(
    private val accountService: AccountService,
    private val listAccountTransactionFeed: ListAccountTransactionFeed,
    private val listAccountFutureTransactionFeed: ListAccountFutureTransactionFeed,
) : AccountApi {
    override suspend fun create(request: CreateAccountRequest): AccountResponse =
        accountService.create(request.toCommand()).toResponse()

    override suspend fun update(id: String, request: UpdateAccountRequest): AccountResponse =
        accountService.update(request.toCommand(id)).toResponse()

    override suspend fun archive(id: String): AccountResponse =
        accountService.archive(id).toResponse()

    override suspend fun unarchive(id: String): AccountResponse =
        accountService.unarchive(id).toResponse()

    override suspend fun list(archived: Boolean): List<AccountQueryResponse> =
        accountService.listQueries(archived).map { it.toResponse() }

    override suspend fun getById(id: String): AccountQueryResponse =
        accountService.getQuery(id).toResponse()

    override suspend fun listTransactions(
        id: String,
        limit: Int,
        before: Long?,
    ): AccountTransactionFeedPageResponse =
        listAccountTransactionFeed(id, before, limit).toResponse()

    override suspend fun listFutureTransactions(
        id: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): AccountFutureTransactionFeedPageResponse =
        listAccountFutureTransactionFeed(id, fromDate, toDate, after, limit).toResponse()
}
