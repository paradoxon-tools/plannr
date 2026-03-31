package de.chennemann.plannr.server.currencies.api

import de.chennemann.plannr.server.currencies.application.CreateCurrency
import de.chennemann.plannr.server.currencies.domain.Currency

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

data class UpdateCurrencyRequest(
    val code: String,
    val name: String,
    val symbol: String,
    val decimalPlaces: Int,
    val symbolPosition: String,
) {
    fun toCommand(pathCode: String): de.chennemann.plannr.server.currencies.application.UpdateCurrency.Command =
        de.chennemann.plannr.server.currencies.application.UpdateCurrency.Command(
            pathCode = pathCode,
            code = code,
            name = name,
            symbol = symbol,
            decimalPlaces = decimalPlaces,
            symbolPosition = symbolPosition,
        )
}

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
