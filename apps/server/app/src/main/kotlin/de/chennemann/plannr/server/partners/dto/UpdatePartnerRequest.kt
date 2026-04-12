package de.chennemann.plannr.server.partners.dto

import de.chennemann.plannr.server.partners.usecases.UpdatePartner

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
