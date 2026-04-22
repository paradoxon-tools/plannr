package de.chennemann.plannr.server.currencies.domain

interface CurrencyTemplateCatalog {
    fun findByCode(code: String): Currency?
}
