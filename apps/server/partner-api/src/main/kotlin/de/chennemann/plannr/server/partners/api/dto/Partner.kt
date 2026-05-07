package de.chennemann.plannr.server.partners.api.dto

data class Partner(
    val id: Long,
    val name: String,
    val description: String?,
    val isArchived: Boolean,
    val createdAt: Long,
)
