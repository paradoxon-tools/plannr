package de.chennemann.plannr.server.currencies.api

import de.chennemann.plannr.server.currencies.application.CreateCurrency
import de.chennemann.plannr.server.currencies.application.GetCurrency
import de.chennemann.plannr.server.currencies.application.ListCurrencies
import de.chennemann.plannr.server.currencies.application.UpdateCurrency
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/currencies")
class CurrencyController(
    private val createCurrency: CreateCurrency,
    private val updateCurrency: UpdateCurrency,
    private val getCurrency: GetCurrency,
    private val listCurrencies: ListCurrencies,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody request: CreateCurrencyRequest): CurrencyResponse =
        CurrencyResponse.from(createCurrency(request.toCommand()))

    @PutMapping("/{code}")
    suspend fun update(@PathVariable code: String, @RequestBody request: UpdateCurrencyRequest): CurrencyResponse =
        CurrencyResponse.from(updateCurrency(request.toCommand(code)))

    @GetMapping("/{code}")
    suspend fun getByCode(@PathVariable code: String): CurrencyResponse =
        CurrencyResponse.from(getCurrency(code))

    @GetMapping
    suspend fun list(): List<CurrencyResponse> =
        listCurrencies().map(CurrencyResponse::from)
}
