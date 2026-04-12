package de.chennemann.plannr.server.currencies.api.dto

import de.chennemann.plannr.server.currencies.usecases.UpdateCurrency

data class UpdateCurrencyRequest(
    val code: String,
    val name: String,
    val symbol: String,
    val decimalPlaces: Int,
    val symbolPosition: String,
) {
    fun toCommand(pathCode: String): UpdateCurrency.Command =
        UpdateCurrency.Command(
            pathCode = pathCode,
            code = code,
            name = name,
            symbol = symbol,
            decimalPlaces = decimalPlaces,
            symbolPosition = symbolPosition,
        )
}
