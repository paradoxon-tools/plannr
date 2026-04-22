package de.chennemann.plannr.server.currencies.service

import de.chennemann.plannr.server.currencies.domain.Currency

interface CurrencyService {
    suspend fun create(command: CreateCurrencyCommand): Currency

    suspend fun update(command: UpdateCurrencyCommand): Currency

    suspend fun list(): List<Currency>

    suspend fun ensureExists(currencyCode: String): Currency
}

data class CreateCurrencyCommand(
    val code: String,
    val name: String,
    val symbol: String,
    val decimalPlaces: Int,
    val symbolPosition: String,
)

data class UpdateCurrencyCommand(
    val pathCode: String,
    val code: String,
    val name: String,
    val symbol: String,
    val decimalPlaces: Int,
    val symbolPosition: String,
)
