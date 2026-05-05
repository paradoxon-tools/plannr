package de.chennemann.plannr.server.accounts.api.dto

data class CreateAccountCommand(
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
)
