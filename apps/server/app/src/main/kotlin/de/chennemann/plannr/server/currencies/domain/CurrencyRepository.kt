package de.chennemann.plannr.server.currencies.domain

interface CurrencyRepository {
    suspend fun save(currency: Currency): Currency
    suspend fun update(currency: Currency): Currency
    suspend fun findByCode(code: String): Currency?
    suspend fun findAll(): List<Currency>
}
