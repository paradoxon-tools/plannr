package de.chennemann.plannr.server.currencies.usecases

import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import org.springframework.stereotype.Component

interface ListCurrenciesQuery {
    suspend operator fun invoke(): List<Currency>
}

@Component
internal class ListCurrenciesQueryUseCase(
    private val currencyRepository: CurrencyRepository,
) : ListCurrenciesQuery {
    override suspend fun invoke(): List<Currency> =
        currencyRepository.findAll()
}
