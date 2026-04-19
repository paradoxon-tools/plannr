package de.chennemann.plannr.server.transactions.api

import de.chennemann.plannr.server.query.transactions.api.TransactionQueryApi
import de.chennemann.plannr.server.transactions.api.dto.TransactionResponse
import de.chennemann.plannr.server.transactions.usecases.ListTransactionsQuery
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionQueryController(
    private val listTransactionsQuery: ListTransactionsQuery,
) : TransactionQueryApi {
    override suspend fun list(accountId: String?, pocketId: String?, archived: Boolean): List<TransactionResponse> =
        listTransactionsQuery(accountId, pocketId, archived).map { it.toResponse() }
}
