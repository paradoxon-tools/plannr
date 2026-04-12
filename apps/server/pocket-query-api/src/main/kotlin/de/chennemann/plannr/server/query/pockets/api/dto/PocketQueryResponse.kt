package de.chennemann.plannr.server.query.pockets.api.dto

data class PocketQueryResponse(
    val id: String,
    val accountId: String,
    val name: String,
    val description: String?,
    val color: Int,
    val isDefault: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
    val currentBalance: Long,
)
