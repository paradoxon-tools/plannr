package de.chennemann.plannr.server.partners.api.dto

data class CreatePartnerCommand(
    val name: String,
    val notes: String?,
)
