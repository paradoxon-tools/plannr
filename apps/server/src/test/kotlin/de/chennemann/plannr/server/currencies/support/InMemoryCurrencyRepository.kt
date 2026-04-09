package de.chennemann.plannr.server.currencies.support

import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository

class InMemoryCurrencyRepository : CurrencyRepository {
    private val currencies = linkedMapOf<String, Currency>()

    override suspend fun save(currency: Currency): Currency {
        currencies[currency.code] = currency
        return currency
    }

    override suspend fun update(currency: Currency): Currency {
        currencies[currency.code] = currency
        return currency
    }

    override suspend fun findByCode(code: String): Currency? = currencies[code]
}
