package de.chennemann.plannr.server.currencies.api

import de.chennemann.plannr.server.currencies.api.dto.CurrencyResponse
import de.chennemann.plannr.server.currencies.usecases.ListCurrenciesQuery
import de.chennemann.plannr.server.query.currencies.api.CurrencyQueryApi
import org.springframework.web.bind.annotation.RestController

@RestController
class CurrencyQueryController(
    private val listCurrenciesQuery: ListCurrenciesQuery,
) : CurrencyQueryApi {
    override suspend fun list(): List<CurrencyResponse> =
        listCurrenciesQuery().map { it.toResponse() }
}
