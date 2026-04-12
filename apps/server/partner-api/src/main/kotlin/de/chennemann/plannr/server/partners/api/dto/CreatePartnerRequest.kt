package de.chennemann.plannr.server.partners.api.dto

data class CreatePartnerRequest(
    val name: String,
    val notes: String?,
)
