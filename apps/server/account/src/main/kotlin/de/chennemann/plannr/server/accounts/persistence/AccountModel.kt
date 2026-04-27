package de.chennemann.plannr.server.accounts.persistence

import de.chennemann.plannr.server.accounts.domain.Account

data class AccountModel(
    val id: String?,
    val name: String,
    val institution: String,
    val currencyCode: String,
    val weekendHandling: String,
    val isArchived: Boolean,
    val createdAt: Long,
)

internal fun AccountModel.toDomain(): Account =
    Account(
        id = requireNotNull(id) { "AccountModel.id must not be null when mapping to domain" },
        name = name,
        institution = institution,
        currencyCode = currencyCode,
        weekendHandling = weekendHandling,
        isArchived = isArchived,
        createdAt = createdAt,
    )

internal fun Account.toModel(): AccountModel =
    AccountModel(
        id = id,
        name = name,
        institution = institution,
        currencyCode = currencyCode,
        weekendHandling = weekendHandling,
        isArchived = isArchived,
        createdAt = createdAt,
    )
