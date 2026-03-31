package de.chennemann.plannr.server.currencies.application

import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import org.springframework.stereotype.Service

@Service
class ListCurrencies(
    private val currencyRepository: CurrencyRepository,
) {
    suspend operator fun invoke(): List<Currency> = currencyRepository.findAll()
}
