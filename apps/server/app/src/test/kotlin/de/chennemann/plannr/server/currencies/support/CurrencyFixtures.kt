package de.chennemann.plannr.server.currencies.support

import de.chennemann.plannr.server.currencies.dto.CreateCurrencyRequest
import de.chennemann.plannr.server.currencies.dto.UpdateCurrencyRequest
import de.chennemann.plannr.server.currencies.usecases.CreateCurrency
import de.chennemann.plannr.server.currencies.usecases.UpdateCurrency
import de.chennemann.plannr.server.currencies.domain.Currency

object CurrencyFixtures {
    const val DEFAULT_CODE = "EUR"
    const val DEFAULT_NAME = "Euro"
    const val DEFAULT_SYMBOL = "€"
    const val DEFAULT_DECIMAL_PLACES = 2
    const val DEFAULT_SYMBOL_POSITION = "before"

    fun currency(
        code: String = DEFAULT_CODE,
        name: String = DEFAULT_NAME,
        symbol: String = DEFAULT_SYMBOL,
        decimalPlaces: Int = DEFAULT_DECIMAL_PLACES,
        symbolPosition: String = DEFAULT_SYMBOL_POSITION,
    ): Currency =
        Currency(
            code = code,
            name = name,
            symbol = symbol,
            decimalPlaces = decimalPlaces,
            symbolPosition = symbolPosition,
        )

    fun createCurrencyCommand(
        code: String = DEFAULT_CODE.lowercase(),
        name: String = DEFAULT_NAME,
        symbol: String = DEFAULT_SYMBOL,
        decimalPlaces: Int = DEFAULT_DECIMAL_PLACES,
        symbolPosition: String = DEFAULT_SYMBOL_POSITION,
    ): CreateCurrency.Command =
        CreateCurrency.Command(
            code = code,
            name = name,
            symbol = symbol,
            decimalPlaces = decimalPlaces,
            symbolPosition = symbolPosition,
        )

    fun createCurrencyRequest(
        code: String = DEFAULT_CODE.lowercase(),
        name: String = DEFAULT_NAME,
        symbol: String = DEFAULT_SYMBOL,
        decimalPlaces: Int = DEFAULT_DECIMAL_PLACES,
        symbolPosition: String = DEFAULT_SYMBOL_POSITION,
    ): CreateCurrencyRequest =
        CreateCurrencyRequest(
            code = code,
            name = name,
            symbol = symbol,
            decimalPlaces = decimalPlaces,
            symbolPosition = symbolPosition,
        )

    fun updateCurrencyCommand(
        pathCode: String = DEFAULT_CODE.lowercase(),
        code: String = DEFAULT_CODE.lowercase(),
        name: String = DEFAULT_NAME,
        symbol: String = DEFAULT_SYMBOL,
        decimalPlaces: Int = DEFAULT_DECIMAL_PLACES,
        symbolPosition: String = DEFAULT_SYMBOL_POSITION,
    ): UpdateCurrency.Command =
        UpdateCurrency.Command(
            pathCode = pathCode,
            code = code,
            name = name,
            symbol = symbol,
            decimalPlaces = decimalPlaces,
            symbolPosition = symbolPosition,
        )

    fun updateCurrencyRequest(
        code: String = DEFAULT_CODE.lowercase(),
        name: String = DEFAULT_NAME,
        symbol: String = DEFAULT_SYMBOL,
        decimalPlaces: Int = DEFAULT_DECIMAL_PLACES,
        symbolPosition: String = DEFAULT_SYMBOL_POSITION,
    ): UpdateCurrencyRequest =
        UpdateCurrencyRequest(
            code = code,
            name = name,
            symbol = symbol,
            decimalPlaces = decimalPlaces,
            symbolPosition = symbolPosition,
        )
}
