package de.chennemann.plannr.server.currencies.api

import de.chennemann.plannr.server.currencies.api.dto.CreateCurrencyRequest
import de.chennemann.plannr.server.currencies.api.dto.CurrencyResponse
import de.chennemann.plannr.server.currencies.api.dto.UpdateCurrencyRequest
import de.chennemann.plannr.server.currencies.service.CurrencyService
import org.springframework.web.bind.annotation.RestController

@RestController
class CurrencyController(
    private val currencyService: CurrencyService,
) : CurrencyApi {
    override suspend fun create(request: CreateCurrencyRequest): CurrencyResponse =
        currencyService.create(request.toCommand()).toResponse()

    override suspend fun update(code: String, request: UpdateCurrencyRequest): CurrencyResponse =
        currencyService.update(request.toCommand(code)).toResponse()

    override suspend fun list(): List<CurrencyResponse> =
        currencyService.list().map { it.toResponse() }
}
