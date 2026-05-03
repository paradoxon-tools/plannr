package de.chennemann.plannr.server.pockets.persistence

import de.chennemann.plannr.server.pockets.domain.Pocket
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import kotlin.jvm.JvmName

@Table("pockets")
data class PocketModel(
    @field:Id
    @get:JvmName("getEntityId")
    val id: String?,
    @Column("account_id")
    val accountId: String,
    val name: String,
    val description: String?,
    val color: Int,
    @Column("is_default")
    val isDefault: Boolean,
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
        accountId: String,
        name: String,
        description: String?,
        color: Int,
        isDefault: Boolean,
        isArchived: Boolean,
        createdAt: Long,
    ) : this(id, accountId, name, description, color, isDefault, isArchived, createdAt, persisted = true)

    override fun getId(): String? = id

    override fun isNew(): Boolean = !persisted

    fun persisted(): PocketModel = copy(persisted = true)
}

internal fun PocketModel.toDomain(): Pocket =
    Pocket(
        id = requireNotNull(id) { "PocketModel.id must not be null when mapping to domain" },
        accountId = accountId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
        isArchived = isArchived,
        createdAt = createdAt,
    )

internal fun Pocket.toModel(): PocketModel =
    PocketModel(
        id = id,
        accountId = accountId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
        isArchived = isArchived,
        createdAt = createdAt,
    )

internal fun Pocket.toPersistedModel(): PocketModel = toModel().persisted()
