package de.chennemann.plannr.server.transactions.materialization.service

import de.chennemann.plannr.server.transactions.materialization.api.dto.UpcomingTransactionsResponse
import java.time.LocalDate

interface UpcomingTransactionService {
    suspend fun getForAccount(
        accountId: Long,
        after: LocalDate?,
        count: Int,
    ): UpcomingTransactionsResponse

    suspend fun getForPocket(
        pocketId: Long,
        after: LocalDate?,
        count: Int,
    ): UpcomingTransactionsResponse

    suspend fun getForContract(
        contractId: Long,
        after: LocalDate?,
        count: Int,
    ): UpcomingTransactionsResponse
}
