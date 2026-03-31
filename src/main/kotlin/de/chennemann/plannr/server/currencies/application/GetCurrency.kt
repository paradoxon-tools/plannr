package de.chennemann.plannr.server.currencies.application

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import org.springframework.stereotype.Service

@Service
class GetCurrency(
    private val currencyRepository: CurrencyRepository,
) {
    suspend operator fun invoke(code: String): Currency =
        currencyRepository.findByCode(code.trim().uppercase())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Currency not found",
                details = mapOf("code" to code.trim().uppercase()),
            )
}
