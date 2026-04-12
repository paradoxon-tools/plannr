package de.chennemann.plannr.server.currencies.api

import de.chennemann.plannr.server.currencies.dto.CreateCurrencyRequest
import de.chennemann.plannr.server.currencies.dto.CurrencyResponse
import de.chennemann.plannr.server.currencies.dto.UpdateCurrencyRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/currencies")
interface CurrencyIngressApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateCurrencyRequest): CurrencyResponse

    @PutExchange("/{code}")
    suspend fun update(@PathVariable code: String, @RequestBody request: UpdateCurrencyRequest): CurrencyResponse
}
