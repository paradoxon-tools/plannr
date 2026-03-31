package de.chennemann.plannr.server.currencies.usecases

import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import org.springframework.stereotype.Component

interface ListCurrencies {
    suspend operator fun invoke(): List<Currency>
}

@Component
internal class ListCurrenciesUseCase(
    private val currencyRepository: CurrencyRepository,
) : ListCurrencies {
    override suspend fun invoke(): List<Currency> = currencyRepository.findAll()
}
