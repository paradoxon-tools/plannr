package de.chennemann.plannr.server.pockets.api.dto

data class Pocket(
    val id: Long,
    val accountId: Long,
    val contractId: Long? = null,
    val name: String,
    val description: String?,
    val color: Int,
    val isDefault: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
)
