package de.chennemann.plannr.server.partners.persistence

import de.chennemann.plannr.server.partners.api.dto.Partner
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("partners")
data class PartnerModel(
    @Id
    val id: Long?,
    val name: String,
    val description: String?,
    @Column("is_archived")
    val isArchived: Boolean,
    @Column("created_at")
    val createdAt: Long,
)

fun PartnerModel.toDTO(): Partner =
    Partner(
        id = requireNotNull(id) { "PartnerModel.id must not be null when mapping to domain" },
        name = name,
        description = description,
        isArchived = isArchived,
        createdAt = createdAt,
    )

fun Partner.toModel(): PartnerModel =
    PartnerModel(
        id = id,
        name = name,
        description = description,
        isArchived = isArchived,
        createdAt = createdAt,
    )
