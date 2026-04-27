package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.api.dto.CreatePocketRequest
import de.chennemann.plannr.server.pockets.api.dto.PocketQueryResponse
import de.chennemann.plannr.server.pockets.api.dto.PocketResponse
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketRequest
import de.chennemann.plannr.server.pockets.service.PocketService
import de.chennemann.plannr.server.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.toResponse
import de.chennemann.plannr.server.transactions.usecases.ListPocketFutureTransactionFeed
import de.chennemann.plannr.server.transactions.usecases.ListPocketTransactionFeed
import org.springframework.web.bind.annotation.RestController

@RestController
class PocketController(
    private val pocketService: PocketService,
    private val listPocketTransactionFeed: ListPocketTransactionFeed,
    private val listPocketFutureTransactionFeed: ListPocketFutureTransactionFeed,
) : PocketApi {
    override suspend fun create(request: CreatePocketRequest): PocketResponse =
        pocketService.create(request.toCommand()).toResponse()

    override suspend fun update(id: String, request: UpdatePocketRequest): PocketResponse =
        pocketService.update(request.toCommand(id)).toResponse()

    override suspend fun archive(id: String): PocketResponse =
        pocketService.archive(id).toResponse()

    override suspend fun unarchive(id: String): PocketResponse =
        pocketService.unarchive(id).toResponse()

    override suspend fun list(accountId: String?, archived: Boolean): List<PocketQueryResponse> =
        pocketService.listQueries(accountId, archived).map { it.toResponse() }

    override suspend fun getById(id: String): PocketQueryResponse =
        pocketService.getQuery(id).toResponse()

    override suspend fun listTransactions(
        id: String,
        limit: Int,
        before: Long?,
    ): PocketTransactionFeedPageResponse =
        listPocketTransactionFeed(id, before, limit).toResponse()

    override suspend fun listFutureTransactions(
        id: String,
        fromDate: String?,
        toDate: String?,
        after: Long?,
        limit: Int,
    ): PocketFutureTransactionFeedPageResponse =
        listPocketFutureTransactionFeed(id, fromDate, toDate, after, limit).toResponse()
}
