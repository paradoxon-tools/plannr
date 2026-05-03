package de.chennemann.plannr.server.partners.persistence

import de.chennemann.plannr.server.partners.domain.Partner
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import kotlin.jvm.JvmName

@Table("partners")
data class PartnerModel(
    @field:Id
    @get:JvmName("getEntityId")
    val id: String?,
    val name: String,
    val notes: String?,
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
        notes: String?,
        isArchived: Boolean,
        createdAt: Long,
    ) : this(id, name, notes, isArchived, createdAt, persisted = true)

    override fun getId(): String? = id

    override fun isNew(): Boolean = !persisted

    fun persisted(): PartnerModel = copy(persisted = true)
}

internal fun PartnerModel.toDomain(): Partner =
    Partner(
        id = requireNotNull(id) { "PartnerModel.id must not be null when mapping to domain" },
        name = name,
        notes = notes,
        isArchived = isArchived,
        createdAt = createdAt,
    )

internal fun Partner.toModel(): PartnerModel =
    PartnerModel(
        id = id,
        name = name,
        notes = notes,
        isArchived = isArchived,
        createdAt = createdAt,
    )

internal fun Partner.toPersistedModel(): PartnerModel = toModel().persisted()
