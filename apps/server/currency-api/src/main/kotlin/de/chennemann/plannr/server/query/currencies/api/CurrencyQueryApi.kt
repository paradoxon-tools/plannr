package de.chennemann.plannr.server.query.currencies.api

import de.chennemann.plannr.server.currencies.api.dto.CurrencyResponse
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange("/query/currencies")
interface CurrencyQueryApi {
    @GetExchange
    suspend fun list(): List<CurrencyResponse>
}
