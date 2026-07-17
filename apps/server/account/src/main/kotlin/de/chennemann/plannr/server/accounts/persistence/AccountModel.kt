package de.chennemann.plannr.server.accounts.persistence

import de.chennemann.plannr.server.accounts.api.dto.Account
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("accounts")
data class AccountModel(
    @Id
    val id: Long?,
    val name: String,
    val institution: String,
    @Column("currency_code")
    val currencyCode: String,
    @Column("weekend_handling")
    val weekendHandling: String,
    @Column("is_archived")
    val isArchived: Boolean,
    @Column("created_at")
    val createdAt: Long,
)

fun AccountModel.toDTO(): Account =
    Account(
        id = requireNotNull(id) { "AccountModel.id must not be null when mapping to domain" },
        name = name,
        institution = institution,
        currencyCode = currencyCode,
        weekendHandling = weekendHandling,
        isArchived = isArchived,
        createdAt = createdAt,
    )

fun Account.toModel(): AccountModel =
    AccountModel(
        id = id,
        name = name,
        institution = institution,
        currencyCode = currencyCode,
        weekendHandling = weekendHandling,
        isArchived = isArchived,
        createdAt = createdAt,
    )
