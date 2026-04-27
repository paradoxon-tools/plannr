package de.chennemann.plannr.server.currencies.domain

import de.chennemann.plannr.server.currencies.persistence.CurrencyModel

interface CurrencyRepository {
    suspend fun save(currency: CurrencyModel): Currency
    suspend fun update(currency: CurrencyModel): Currency
    suspend fun findByCode(code: String): Currency?
    suspend fun findAll(): List<Currency>
}
