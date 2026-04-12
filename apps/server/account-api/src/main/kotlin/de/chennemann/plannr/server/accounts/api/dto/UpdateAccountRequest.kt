package de.chennemann.plannr.server.accounts.api.dto

data class UpdateAccountRequest(
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
)
