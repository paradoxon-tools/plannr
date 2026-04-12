package de.chennemann.plannr.server.currencies.api.dto

import de.chennemann.plannr.server.currencies.usecases.CreateCurrency

data class CreateCurrencyRequest(
    val code: String,
    val name: String,
    val symbol: String,
    val decimalPlaces: Int,
    val symbolPosition: String,
) {
    fun toCommand(): CreateCurrency.Command =
        CreateCurrency.Command(
            code = code,
            name = name,
            symbol = symbol,
            decimalPlaces = decimalPlaces,
            symbolPosition = symbolPosition,
        )
}
