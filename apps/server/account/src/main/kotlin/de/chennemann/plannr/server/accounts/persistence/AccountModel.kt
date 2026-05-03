package de.chennemann.plannr.server.accounts.persistence

import de.chennemann.plannr.server.accounts.domain.Account
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import kotlin.jvm.JvmName

@Table("accounts")
data class AccountModel(
    @field:Id
    @get:JvmName("getEntityId")
    val id: String?,
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
    @Transient
    val persisted: Boolean = false,
) : Persistable<String> {
    @PersistenceCreator
    constructor(
        id: String?,
        name: String,
        institution: String,
        currencyCode: String,
        weekendHandling: String,
        isArchived: Boolean,
        createdAt: Long,
    ) : this(id, name, institution, currencyCode, weekendHandling, isArchived, createdAt, persisted = true)

    override fun getId(): String? = id

    override fun isNew(): Boolean = !persisted

    fun persisted(): AccountModel = copy(persisted = true)
}

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

internal fun Account.toPersistedModel(): AccountModel = toModel().persisted()
