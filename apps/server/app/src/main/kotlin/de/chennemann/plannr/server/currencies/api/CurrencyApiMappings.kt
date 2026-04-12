package de.chennemann.plannr.server.currencies.api

import de.chennemann.plannr.server.currencies.api.dto.CreateCurrencyRequest
import de.chennemann.plannr.server.currencies.api.dto.CurrencyResponse
import de.chennemann.plannr.server.currencies.api.dto.UpdateCurrencyRequest
import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.usecases.CreateCurrency
import de.chennemann.plannr.server.currencies.usecases.UpdateCurrency

internal fun CreateCurrencyRequest.toCommand(): CreateCurrency.Command =
    CreateCurrency.Command(
        code = code,
        name = name,
        symbol = symbol,
        decimalPlaces = decimalPlaces,
        symbolPosition = symbolPosition,
    )

internal fun UpdateCurrencyRequest.toCommand(pathCode: String): UpdateCurrency.Command =
    UpdateCurrency.Command(
        pathCode = pathCode,
        code = code,
        name = name,
        symbol = symbol,
        decimalPlaces = decimalPlaces,
        symbolPosition = symbolPosition,
    )

internal fun Currency.toResponse(): CurrencyResponse =
    CurrencyResponse(
        code = code,
        name = name,
        symbol = symbol,
        decimalPlaces = decimalPlaces,
        symbolPosition = symbolPosition,
    )
