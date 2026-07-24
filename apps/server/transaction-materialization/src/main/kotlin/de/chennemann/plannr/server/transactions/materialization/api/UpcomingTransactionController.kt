package de.chennemann.plannr.server.transactions.materialization.api

import de.chennemann.plannr.server.transactions.materialization.api.dto.UpcomingTransactionsResponse
import de.chennemann.plannr.server.transactions.materialization.service.UpcomingTransactionService
import java.time.LocalDate
import org.springframework.web.bind.annotation.RestController

@RestController
class UpcomingTransactionController(
    private val upcomingTransactionService: UpcomingTransactionService,
) : UpcomingTransactionApi {
    override suspend fun getForAccount(
        id: Long,
        after: LocalDate?,
        count: Int,
    ): UpcomingTransactionsResponse =
        upcomingTransactionService.getForAccount(id, after, count)

    override suspend fun getForPocket(
        id: Long,
        after: LocalDate?,
        count: Int,
    ): UpcomingTransactionsResponse =
        upcomingTransactionService.getForPocket(id, after, count)

    override suspend fun getForContract(
        id: Long,
        after: LocalDate?,
        count: Int,
    ): UpcomingTransactionsResponse =
        upcomingTransactionService.getForPocket(id, after, count)
}
