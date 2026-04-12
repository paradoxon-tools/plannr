package de.chennemann.plannr.server.partners.api.dto

data class UpdatePartnerRequest(
    val name: String,
    val notes: String?,
)
