package de.chennemann.plannr.server.partners.api.dto

import de.chennemann.plannr.server.partners.domain.Partner

data class PartnerResponse(
    val id: String,
    val name: String,
    val notes: String?,
    val isArchived: Boolean,
    val createdAt: Long,
) {
    companion object {
        fun from(partner: Partner): PartnerResponse =
            PartnerResponse(
                id = partner.id,
                name = partner.name,
                notes = partner.notes,
                isArchived = partner.isArchived,
                createdAt = partner.createdAt,
            )
    }
}
