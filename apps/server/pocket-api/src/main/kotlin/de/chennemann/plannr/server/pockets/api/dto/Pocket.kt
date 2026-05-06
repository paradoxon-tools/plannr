package de.chennemann.plannr.server.pockets.api.dto

data class Pocket(
    val id: String,
    val accountId: Long,
    val name: String,
    val description: String?,
    val color: Int,
    val isDefault: Boolean,
    val isContractPocket: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
)
