package de.chennemann.plannr.server.currencies.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import org.springframework.stereotype.Component

interface GetCurrency {
    suspend operator fun invoke(code: String): Currency
}

@Component
internal class GetCurrencyUseCase(
    private val currencyRepository: CurrencyRepository,
) : GetCurrency {
    override suspend fun invoke(code: String): Currency =
        currencyRepository.findByCode(code.trim().uppercase())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Currency not found",
                details = mapOf("code" to code.trim().uppercase()),
            )
}
