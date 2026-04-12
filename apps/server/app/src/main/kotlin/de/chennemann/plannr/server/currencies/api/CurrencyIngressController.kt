package de.chennemann.plannr.server.currencies.api

import de.chennemann.plannr.server.currencies.usecases.CreateCurrency
import de.chennemann.plannr.server.currencies.usecases.UpdateCurrency
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/currencies")
class CurrencyIngressController(
    private val createCurrency: CreateCurrency,
    private val updateCurrency: UpdateCurrency,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateCurrencyRequest): CurrencyResponse =
        CurrencyResponse.from(createCurrency(request.toCommand()))

    @PutMapping("/{code}")
    suspend fun update(@PathVariable code: String, @RequestBody request: UpdateCurrencyRequest): CurrencyResponse =
        CurrencyResponse.from(updateCurrency(request.toCommand(code)))

}
