package de.chennemann.plannr.server.query.accounts.api.dto

data class AccountQueryResponse(
    val id: String,
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
    val isArchived: Boolean,
    val createdAt: Long,
    val currentBalance: Long,
)
