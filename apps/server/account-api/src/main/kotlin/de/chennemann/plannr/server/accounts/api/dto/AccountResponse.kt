package de.chennemann.plannr.server.accounts.api.dto

data class AccountResponse(
    val id: String,
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
    val isArchived: Boolean,
    val createdAt: Long,
)
