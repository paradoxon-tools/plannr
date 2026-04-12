package de.chennemann.plannr.server.currencies.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyRepository
import de.chennemann.plannr.server.currencies.domain.CurrencyTemplateCatalog
import org.springframework.stereotype.Component

interface EnsureCurrencyExists {
    suspend operator fun invoke(currencyCode: String): Currency
}

@Component
internal class EnsureCurrencyExistsUseCase(
    private val currencyRepository: CurrencyRepository,
    private val currencyTemplateCatalog: CurrencyTemplateCatalog,
) : EnsureCurrencyExists {
    override suspend fun invoke(currencyCode: String): Currency {
        val normalizedCode = currencyCode.trim().uppercase()

        currencyRepository.findByCode(normalizedCode)?.let { return it }

        val template = currencyTemplateCatalog.findByCode(normalizedCode)
            ?: throw NotFoundException(
                code = "not_found",
                message = "Currency not found",
                details = mapOf("code" to normalizedCode),
            )

        return currencyRepository.save(template)
    }
}
