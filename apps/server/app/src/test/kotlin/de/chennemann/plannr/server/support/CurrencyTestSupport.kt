package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.service.CreateCurrencyCommand
import de.chennemann.plannr.server.currencies.service.CurrencyService
import de.chennemann.plannr.server.currencies.service.UpdateCurrencyCommand

object TestCurrencies {
    fun eur(): Currency =
        Currency(
            code = "EUR",
            name = "Euro",
            symbol = "EUR",
            decimalPlaces = 2,
            symbolPosition = "before",
        )

    fun usd(): Currency =
        Currency(
            code = "USD",
            name = "US Dollar",
            symbol = "$",
            decimalPlaces = 2,
            symbolPosition = "before",
        )
}

class FakeCurrencyService(
    initialCurrencies: Iterable<Currency> = listOf(TestCurrencies.eur()),
    private val templates: Map<String, Currency> = emptyMap(),
) : CurrencyService {
    private val currencies = initialCurrencies.associateByTo(linkedMapOf()) { it.code }

    override suspend fun create(command: CreateCurrencyCommand): Currency {
        val currency = command.toCurrency()
        currencies[currency.code] = currency
        return currency
    }

    override suspend fun update(command: UpdateCurrencyCommand): Currency {
        val currency = Currency(
            code = command.code,
            name = command.name,
            symbol = command.symbol,
            decimalPlaces = command.decimalPlaces,
            symbolPosition = command.symbolPosition,
        )
        currencies[currency.code] = currency
        return currency
    }

    override suspend fun list(): List<Currency> =
        currencies.values.sortedBy { it.code }

    override suspend fun ensureExists(currencyCode: String): Currency {
        val normalizedCode = currencyCode.trim().uppercase()
        currencies[normalizedCode]?.let { return it }
        templates[normalizedCode]?.let {
            currencies[normalizedCode] = it
            return it
        }
        throw NotFoundException("not_found", "Currency not found", mapOf("code" to normalizedCode))
    }

    suspend fun findByCode(code: String): Currency? =
        currencies[code.trim().uppercase()]

    private fun CreateCurrencyCommand.toCurrency(): Currency =
        Currency(
            code = code,
            name = name,
            symbol = symbol,
            decimalPlaces = decimalPlaces,
            symbolPosition = symbolPosition,
        )
}
