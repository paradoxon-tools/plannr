package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.api.AccountFutureTransactionFeedQuery
import de.chennemann.plannr.server.accounts.api.AccountTransactionFeedQuery
import de.chennemann.plannr.server.accounts.service.AccountArchiveCascade
import de.chennemann.plannr.server.accounts.service.AccountBalanceProvider
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.service.CreateCurrencyCommand
import de.chennemann.plannr.server.currencies.service.CurrencyService
import de.chennemann.plannr.server.currencies.service.UpdateCurrencyCommand
import de.chennemann.plannr.server.transactions.api.dto.AccountFutureTransactionFeedPageResponse
import de.chennemann.plannr.server.transactions.api.dto.AccountTransactionFeedPageResponse
import org.springframework.context.annotation.Bean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(scanBasePackages = ["de.chennemann.plannr.server"])
@EnableR2dbcRepositories(basePackages = ["de.chennemann.plannr.server"])
class AccountTestApplication {
    @Bean
    fun currencyService(): CurrencyService =
        object : CurrencyService {
            override suspend fun create(command: CreateCurrencyCommand): Currency =
                Currency(command.code, command.name, command.symbol, command.decimalPlaces, command.symbolPosition)

            override suspend fun update(command: UpdateCurrencyCommand): Currency =
                Currency(command.code, command.name, command.symbol, command.decimalPlaces, command.symbolPosition)

            override suspend fun list(): List<Currency> = emptyList()

            override suspend fun ensureExists(currencyCode: String): Currency =
                if (currencyCode.trim().uppercase() == "EUR") {
                    Currency("EUR", "Euro", "EUR", 2, "before")
                } else {
                    throw NotFoundException("not_found", "Currency not found", mapOf("code" to currencyCode.trim().uppercase()))
                }
        }

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
                AccountTransactionFeedPageResponse(items = emptyList(), nextBefore = null)
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
                AccountFutureTransactionFeedPageResponse(items = emptyList(), nextAfter = null)
        }
}
