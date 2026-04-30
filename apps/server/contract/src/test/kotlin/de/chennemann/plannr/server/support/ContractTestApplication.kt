package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.accounts.api.AccountFutureTransactionFeedQuery
import de.chennemann.plannr.server.accounts.api.AccountTransactionFeedQuery
import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.service.AccountArchiveCascade
import de.chennemann.plannr.server.accounts.service.AccountBalanceProvider
import de.chennemann.plannr.server.pockets.api.PocketFutureTransactionFeedQuery
import de.chennemann.plannr.server.pockets.api.PocketTransactionFeedQuery
import de.chennemann.plannr.server.contracts.api.ContractFutureTransactionFeedQuery
import de.chennemann.plannr.server.contracts.api.ContractHistoricalTransactionFeedQuery
import de.chennemann.plannr.server.contracts.usecases.ContractRecurringTransactionCascade
import de.chennemann.plannr.server.transactions.api.dto.AccountFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.AccountTransactionFeedPageResponse
import de.chennemann.plannr.server.pockets.domain.Pocket
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
class ContractTestApplication {
    @Bean
    fun accountArchiveCascade(): AccountArchiveCascade =
        object : AccountArchiveCascade {
            override suspend fun archiveFor(account: Account) = Unit
            override suspend fun unarchiveFor(account: Account) = Unit
        }

    @Bean
    fun accountBalanceProvider(): AccountBalanceProvider =
        AccountBalanceProvider { 0L }

    @Bean
    fun accountTransactionFeedQuery(): AccountTransactionFeedQuery =
        object : AccountTransactionFeedQuery {
            override suspend fun list(accountId: String, limit: Int, before: Long?): AccountTransactionFeedPageResponse =
                AccountTransactionFeedPageResponse(
                    items = emptyList(),
                    nextBefore = null,
                )
        }

    @Bean
    fun accountFutureTransactionFeedQuery(): AccountFutureTransactionFeedQuery =
        object : AccountFutureTransactionFeedQuery {
            override suspend fun list(
                accountId: String,
                fromDate: String?,
                toDate: String?,
                after: Long?,
                limit: Int,
            ): AccountFutureTransactionFeedPageResponse =
                AccountFutureTransactionFeedPageResponse(
                    items = emptyList(),
                    nextAfter = null,
                )
        }

    @Bean
    fun pocketAccountLookup(): PocketAccountLookup =
        PocketAccountLookup { true }

    @Bean
    fun pocketTransactionFeedQuery(): PocketTransactionFeedQuery =
        object : PocketTransactionFeedQuery {
            override suspend fun list(pocketId: String, limit: Int, before: Long?): PocketTransactionFeedPageResponse =
                PocketTransactionFeedPageResponse(
                    items = emptyList(),
                    nextBefore = null,
                )
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
                PocketFutureTransactionFeedPageResponse(
                    items = emptyList(),
                    nextAfter = null,
                )
        }

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
    fun contractRecurringTransactionCascade(): ContractRecurringTransactionCascade =
        object : ContractRecurringTransactionCascade {
            override suspend fun archiveFor(contract: de.chennemann.plannr.server.contracts.domain.Contract) = Unit
            override suspend fun unarchiveFor(contract: de.chennemann.plannr.server.contracts.domain.Contract) = Unit
        }

    @Bean
    fun contractHistoricalTransactionFeedQuery(): ContractHistoricalTransactionFeedQuery =
        object : ContractHistoricalTransactionFeedQuery {
            override suspend fun list(contractId: String, limit: Int, before: Long?): PocketTransactionFeedPageResponse =
                PocketTransactionFeedPageResponse(
                    items = emptyList(),
                    nextBefore = null,
                )
        }

    @Bean
    fun contractFutureTransactionFeedQuery(): ContractFutureTransactionFeedQuery =
        object : ContractFutureTransactionFeedQuery {
            override suspend fun list(
                contractId: String,
                fromDate: String?,
                toDate: String?,
                after: Long?,
                limit: Int,
            ): PocketFutureTransactionFeedPageResponse =
                PocketFutureTransactionFeedPageResponse(
                    items = emptyList(),
                    nextAfter = null,
                )
        }
}
