package de.chennemann.plannr.server.partners.persistence

import de.chennemann.plannr.server.partners.domain.Partner

data class PartnerModel(
    val id: String?,
    val name: String,
    val notes: String?,
    val isArchived: Boolean,
    val createdAt: Long,
)

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
