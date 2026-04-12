package de.chennemann.plannr.server.currencies.dto

import de.chennemann.plannr.server.currencies.domain.Currency

data class CurrencyResponse(
    val code: String,
    val name: String,
    val symbol: String,
    val decimalPlaces: Int,
    val symbolPosition: String,
) {
    companion object {
        fun from(currency: Currency): CurrencyResponse =
            CurrencyResponse(
                code = currency.code,
                name = currency.name,
                symbol = currency.symbol,
                decimalPlaces = currency.decimalPlaces,
                symbolPosition = currency.symbolPosition,
            )
    }
}
