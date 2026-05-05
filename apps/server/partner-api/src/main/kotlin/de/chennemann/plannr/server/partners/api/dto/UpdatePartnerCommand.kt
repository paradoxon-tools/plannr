package de.chennemann.plannr.server.partners.api.dto

data class UpdatePartnerCommand(
    val id: String,
    val name: String,
    val notes: String?,
)
