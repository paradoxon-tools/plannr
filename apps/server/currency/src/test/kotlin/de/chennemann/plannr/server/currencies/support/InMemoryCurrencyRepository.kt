package de.chennemann.plannr.server.currencies.support

import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import de.chennemann.plannr.server.currencies.persistence.CurrencyModel
import de.chennemann.plannr.server.currencies.persistence.toDomain

class InMemoryCurrencyRepository : CurrencyRepository {
    private val currencies = linkedMapOf<String, Currency>()

    override suspend fun save(currency: CurrencyModel): Currency {
        val persisted = currency.toDomain()
        currencies[persisted.code] = persisted
        return persisted
    }

    override suspend fun update(currency: CurrencyModel): Currency {
        val persisted = currency.toDomain()
        currencies[persisted.code] = persisted
        return persisted
    }

    override suspend fun findByCode(code: String): Currency? = currencies[code]

    override suspend fun findAll(): List<Currency> = currencies.values.sortedBy { it.code }
}
