package de.chennemann.plannr.server.currencies.persistence

import de.chennemann.plannr.server.currencies.domain.Currency

data class CurrencyModel(
    val code: String,
    val name: String,
    val symbol: String,
    val decimalPlaces: Int,
    val symbolPosition: String,
)

internal fun CurrencyModel.toDomain(): Currency =
    Currency(
        code = code,
        name = name,
        symbol = symbol,
        decimalPlaces = decimalPlaces,
        symbolPosition = symbolPosition,
    )

internal fun Currency.toModel(): CurrencyModel =
    CurrencyModel(
        code = code,
        name = name,
        symbol = symbol,
        decimalPlaces = decimalPlaces,
        symbolPosition = symbolPosition,
    )
