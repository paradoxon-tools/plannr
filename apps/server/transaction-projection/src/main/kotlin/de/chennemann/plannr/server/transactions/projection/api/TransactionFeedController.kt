package de.chennemann.plannr.server.transactions.projection.api

import de.chennemann.plannr.server.transactions.projection.api.dto.TransactionFeedResponse
import de.chennemann.plannr.server.transactions.projection.service.TransactionFeedService
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionFeedController(
    private val transactionFeedService: TransactionFeedService,
) : TransactionFeedApi {
    override suspend fun getForAccount(id: Long, cursor: String?, limit: Int): TransactionFeedResponse =
        transactionFeedService.getForAccount(id, cursor, limit)

    override suspend fun getForPocket(id: Long, cursor: String?, limit: Int): TransactionFeedResponse =
        transactionFeedService.getForPocket(id, cursor, limit)

    override suspend fun getForContract(id: Long, cursor: String?, limit: Int): TransactionFeedResponse =
        transactionFeedService.getForContract(id, cursor, limit)
}
