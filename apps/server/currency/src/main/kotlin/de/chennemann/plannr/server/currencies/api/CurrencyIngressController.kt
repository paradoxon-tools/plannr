package de.chennemann.plannr.server.currencies.api

import de.chennemann.plannr.server.currencies.api.dto.CreateCurrencyRequest
import de.chennemann.plannr.server.currencies.api.dto.CurrencyResponse
import de.chennemann.plannr.server.currencies.api.dto.UpdateCurrencyRequest
import de.chennemann.plannr.server.currencies.service.CurrencyService
import org.springframework.web.bind.annotation.RestController

@RestController
class CurrencyIngressController(
    private val currencyService: CurrencyService,
) : CurrencyIngressApi {
    override suspend fun create(request: CreateCurrencyRequest): CurrencyResponse =
        currencyService.create(request.toCommand()).toResponse()

    override suspend fun update(code: String, request: UpdateCurrencyRequest): CurrencyResponse =
        currencyService.update(request.toCommand(code)).toResponse()
}
