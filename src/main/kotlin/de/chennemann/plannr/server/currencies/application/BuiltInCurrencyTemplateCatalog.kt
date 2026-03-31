package de.chennemann.plannr.server.currencies.application

import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyTemplateCatalog
import org.springframework.stereotype.Component

@Component
class BuiltInCurrencyTemplateCatalog : CurrencyTemplateCatalog {
    private val templates = mapOf(
        "EUR" to Currency("EUR", "Euro", "€", 2, "before"),
        "USD" to Currency("USD", "US Dollar", "$", 2, "before"),
        "GBP" to Currency("GBP", "British Pound", "£", 2, "before"),
        "CHF" to Currency("CHF", "Swiss Franc", "CHF", 2, "before"),
        "JPY" to Currency("JPY", "Japanese Yen", "¥", 0, "before"),
    )

    override fun findByCode(code: String): Currency? = templates[code.trim().uppercase()]
}
