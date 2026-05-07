package de.chennemann.plannr.server.partners.api.dto

data class UpdatePartnerCommand(
    val id: Long,
    val name: String,
    val description: String?,
)
