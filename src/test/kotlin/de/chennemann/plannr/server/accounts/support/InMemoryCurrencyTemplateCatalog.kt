package de.chennemann.plannr.server.accounts.support

import de.chennemann.plannr.server.currencies.domain.Currency
import de.chennemann.plannr.server.currencies.domain.CurrencyTemplateCatalog

class InMemoryCurrencyTemplateCatalog(
    private val templates: Map<String, Currency> = emptyMap(),
) : CurrencyTemplateCatalog {
    override fun findByCode(code: String): Currency? = templates[code.trim().uppercase()]
}
