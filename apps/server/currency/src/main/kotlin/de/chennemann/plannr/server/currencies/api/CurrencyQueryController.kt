package de.chennemann.plannr.server.currencies.api

import de.chennemann.plannr.server.currencies.api.dto.CurrencyResponse
import de.chennemann.plannr.server.currencies.service.CurrencyService
import de.chennemann.plannr.server.query.currencies.api.CurrencyQueryApi
import org.springframework.web.bind.annotation.RestController

@RestController
class CurrencyQueryController(
    private val currencyService: CurrencyService,
) : CurrencyQueryApi {
    override suspend fun list(): List<CurrencyResponse> =
        currencyService.list().map { it.toResponse() }
}
