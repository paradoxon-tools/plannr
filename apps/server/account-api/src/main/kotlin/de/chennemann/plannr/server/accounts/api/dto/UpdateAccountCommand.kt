package de.chennemann.plannr.server.accounts.api.dto

data class UpdateAccountCommand(
    val id: Long,
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
)
