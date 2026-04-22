package de.chennemann.plannr.server.accounts.service

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import de.chennemann.plannr.server.accounts.support.AccountFixtures
import de.chennemann.plannr.server.accounts.support.InMemoryAccountRepository
import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.service.CreateCurrencyCommand
import de.chennemann.plannr.server.currencies.service.CurrencyService
import de.chennemann.plannr.server.currencies.service.UpdateCurrencyCommand

internal fun accountService(
    accountRepository: AccountRepository = InMemoryAccountRepository(),
    currencyService: CurrencyService = FakeCurrencyService(),
    archiveCascade: AccountArchiveCascade = NoOpAccountArchiveCascade,
    balanceProvider: AccountBalanceProvider = AccountBalanceProvider { 0 },
): AccountService =
    AccountServiceImpl(
        accountRepository = accountRepository,
        currencyService = currencyService,
        archiveCascade = archiveCascade,
        balanceProvider = balanceProvider,
        accountIdGenerator = { AccountFixtures.DEFAULT_ID },
        timeProvider = { AccountFixtures.DEFAULT_CREATED_AT },
    )

private object NoOpAccountArchiveCascade : AccountArchiveCascade {
    override suspend fun archiveFor(account: Account) = Unit
    override suspend fun unarchiveFor(account: Account) = Unit
}

internal class RecordingAccountArchiveCascade : AccountArchiveCascade {
    val archivedAccountIds = mutableListOf<String>()
    val unarchivedAccountIds = mutableListOf<String>()

    override suspend fun archiveFor(account: Account) {
        archivedAccountIds += account.id
    }

    override suspend fun unarchiveFor(account: Account) {
        unarchivedAccountIds += account.id
    }
}

internal object TestCurrencies {
    fun eur(): Currency = Currency("EUR", "Euro", "EUR", 2, "after")
    fun usd(): Currency = Currency("USD", "US Dollar", "$", 2, "before")
}

internal class FakeCurrencyService(
    initialCurrencies: Iterable<Currency> = listOf(TestCurrencies.eur()),
    private val templates: Map<String, Currency> = emptyMap(),
) : CurrencyService {
    private val currencies = initialCurrencies.associateByTo(linkedMapOf()) { it.code }

    override suspend fun create(command: CreateCurrencyCommand): Currency {
        val currency = Currency(command.code, command.name, command.symbol, command.decimalPlaces, command.symbolPosition)
        currencies[currency.code] = currency
        return currency
    }

    override suspend fun update(command: UpdateCurrencyCommand): Currency {
        currencies.remove(command.pathCode.trim().uppercase())
        return create(
            CreateCurrencyCommand(
                code = command.code,
                name = command.name,
                symbol = command.symbol,
                decimalPlaces = command.decimalPlaces,
                symbolPosition = command.symbolPosition,
            ),
        )
    }

    override suspend fun list(): List<Currency> = currencies.values.toList()

    override suspend fun ensureExists(currencyCode: String): Currency {
        val code = currencyCode.trim().uppercase()
        currencies[code]?.let { return it }
        templates[code]?.let {
            currencies[code] = it
            return it
        }
        throw NotFoundException("not_found", "Currency not found", mapOf("code" to code))
    }

    suspend fun findByCode(code: String): Currency? = currencies[code.trim().uppercase()]
}
