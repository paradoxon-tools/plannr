package de.chennemann.plannr.server.accounts.api.dto

data class UpdateAccountCommand(
    val id: String,
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
)
