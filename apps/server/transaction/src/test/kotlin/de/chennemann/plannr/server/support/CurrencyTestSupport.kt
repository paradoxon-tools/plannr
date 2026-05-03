package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.service.CreateCurrencyCommand
import de.chennemann.plannr.server.currencies.service.CurrencyService
import de.chennemann.plannr.server.currencies.service.UpdateCurrencyCommand

object TestCurrencies {
    fun eur(): Currency = Currency("EUR", "Euro", "EUR", 2, "before")
}

class FakeCurrencyService(
    initialCurrencies: Iterable<Currency> = listOf(TestCurrencies.eur()),
    private val templates: Map<String, Currency> = emptyMap(),
) : CurrencyService {
    private val currencies = initialCurrencies.associateByTo(linkedMapOf()) { it.code }

    override suspend fun create(command: CreateCurrencyCommand): Currency =
        Currency(command.code, command.name, command.symbol, command.decimalPlaces, command.symbolPosition)
            .also { currencies[it.code] = it }

    override suspend fun update(command: UpdateCurrencyCommand): Currency =
        Currency(command.code, command.name, command.symbol, command.decimalPlaces, command.symbolPosition)
            .also { currencies[it.code] = it }

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
}
