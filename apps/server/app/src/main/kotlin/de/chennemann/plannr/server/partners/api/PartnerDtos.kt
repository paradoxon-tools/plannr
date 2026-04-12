package de.chennemann.plannr.server.partners.api

import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.usecases.CreatePartner
import de.chennemann.plannr.server.partners.usecases.UpdatePartner

data class CreatePartnerRequest(
    val name: String,
    val notes: String?,
) {
    fun toCommand(): CreatePartner.Command =
        CreatePartner.Command(
            name = name,
            notes = notes,
        )
}

data class UpdatePartnerRequest(
    val name: String,
    val notes: String?,
) {
    fun toCommand(id: String): UpdatePartner.Command =
        UpdatePartner.Command(
            id = id,
            name = name,
            notes = notes,
        )
}

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
