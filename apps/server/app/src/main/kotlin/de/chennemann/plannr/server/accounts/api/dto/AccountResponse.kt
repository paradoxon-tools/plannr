package de.chennemann.plannr.server.accounts.api.dto

import de.chennemann.plannr.server.accounts.domain.Account

data class AccountResponse(
    val id: String,
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
    val isArchived: Boolean,
    val createdAt: Long,
) {
    companion object {
        fun from(account: Account): AccountResponse =
            AccountResponse(
                id = account.id,
                name = account.name,
                institution = account.institution,
                currencyCode = account.currencyCode,
                weekendHandling = account.weekendHandling,
                isArchived = account.isArchived,
                createdAt = account.createdAt,
            )
    }
}
