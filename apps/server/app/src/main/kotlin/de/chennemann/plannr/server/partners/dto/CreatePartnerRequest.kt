package de.chennemann.plannr.server.partners.dto

import de.chennemann.plannr.server.partners.usecases.CreatePartner

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
