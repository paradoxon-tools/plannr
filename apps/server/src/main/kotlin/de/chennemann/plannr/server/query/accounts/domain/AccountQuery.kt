package de.chennemann.plannr.server.query.accounts.domain

data class AccountQuery(
    val accountId: String,
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
    val isArchived: Boolean,
    val createdAt: Long,
    val currentBalance: Long,
)
