package de.chennemann.plannr.server.partners.api.dto

data class Partner(
    val id: String,
    val name: String,
    val notes: String?,
    val isArchived: Boolean,
    val createdAt: Long,
)
