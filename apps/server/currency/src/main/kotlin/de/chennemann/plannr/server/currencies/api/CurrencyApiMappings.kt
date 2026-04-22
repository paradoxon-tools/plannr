package de.chennemann.plannr.server.currencies.api

import de.chennemann.plannr.server.currencies.api.dto.CreateCurrencyRequest
import de.chennemann.plannr.server.currencies.api.dto.CurrencyResponse
import de.chennemann.plannr.server.currencies.api.dto.UpdateCurrencyRequest
import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.service.CreateCurrencyCommand
import de.chennemann.plannr.server.currencies.service.UpdateCurrencyCommand

internal fun CreateCurrencyRequest.toCommand(): CreateCurrencyCommand =
    CreateCurrencyCommand(
        code = code,
        name = name,
        symbol = symbol,
        decimalPlaces = decimalPlaces,
        symbolPosition = symbolPosition,
    )

internal fun UpdateCurrencyRequest.toCommand(pathCode: String): UpdateCurrencyCommand =
    UpdateCurrencyCommand(
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
