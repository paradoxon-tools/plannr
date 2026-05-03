package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.api.PocketFutureTransactionFeedQuery
import de.chennemann.plannr.server.pockets.api.PocketTransactionFeedQuery
import de.chennemann.plannr.server.pockets.service.PocketAccountLookup
import de.chennemann.plannr.server.pockets.service.PocketArchiveCascade
import de.chennemann.plannr.server.pockets.service.PocketBalanceProvider
import de.chennemann.plannr.server.transactions.api.dto.PocketFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.PocketTransactionFeedPageResponse
import org.springframework.context.annotation.Bean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(scanBasePackages = ["de.chennemann.plannr.server"])
@EnableR2dbcRepositories(basePackages = ["de.chennemann.plannr.server"])
class PocketTestApplication {
    @Bean
    fun pocketAccountLookup(): PocketAccountLookup =
        PocketAccountLookup { true }

    @Bean
    fun pocketArchiveCascade(): PocketArchiveCascade =
        object : PocketArchiveCascade {
            override suspend fun archiveFor(pocket: Pocket) = Unit

            override suspend fun unarchiveFor(pocket: Pocket) = Unit
        }

    @Bean
    fun pocketBalanceProvider(): PocketBalanceProvider =
        PocketBalanceProvider { 0L }

    @Bean
    fun pocketTransactionFeedQuery(): PocketTransactionFeedQuery =
        object : PocketTransactionFeedQuery {
            override suspend fun list(pocketId: String, limit: Int, before: Long?): PocketTransactionFeedPageResponse =
                PocketTransactionFeedPageResponse(items = emptyList(), nextBefore = null)
        }

    @Bean
    fun pocketFutureTransactionFeedQuery(): PocketFutureTransactionFeedQuery =
        object : PocketFutureTransactionFeedQuery {
            override suspend fun list(
                pocketId: String,
                fromDate: String?,
                toDate: String?,
                after: Long?,
                limit: Int,
            ): PocketFutureTransactionFeedPageResponse =
                PocketFutureTransactionFeedPageResponse(items = emptyList(), nextAfter = null)
        }
}
