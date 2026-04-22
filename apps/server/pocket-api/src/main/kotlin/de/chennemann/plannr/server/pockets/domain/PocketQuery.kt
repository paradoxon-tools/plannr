package de.chennemann.plannr.server.pockets.domain

data class PocketQuery(
    val pocketId: String,
    val accountId: String,
    val name: String,
    val description: String?,
    val color: Int,
    val isDefault: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
    val currentBalance: Long,
)
