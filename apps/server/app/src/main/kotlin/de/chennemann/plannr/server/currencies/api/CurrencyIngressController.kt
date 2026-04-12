package de.chennemann.plannr.server.currencies.api

import de.chennemann.plannr.server.currencies.api.dto.CreateCurrencyRequest
import de.chennemann.plannr.server.currencies.api.dto.CurrencyResponse
import de.chennemann.plannr.server.currencies.api.dto.UpdateCurrencyRequest
import de.chennemann.plannr.server.currencies.usecases.CreateCurrency
import de.chennemann.plannr.server.currencies.usecases.UpdateCurrency
import org.springframework.web.bind.annotation.RestController

@RestController
class CurrencyIngressController(
    private val createCurrency: CreateCurrency,
    private val updateCurrency: UpdateCurrency,
) : CurrencyIngressApi {
    override suspend fun create(request: CreateCurrencyRequest): CurrencyResponse =
        createCurrency(request.toCommand()).toResponse()

    override suspend fun update(code: String, request: UpdateCurrencyRequest): CurrencyResponse =
        updateCurrency(request.toCommand(code)).toResponse()
}
