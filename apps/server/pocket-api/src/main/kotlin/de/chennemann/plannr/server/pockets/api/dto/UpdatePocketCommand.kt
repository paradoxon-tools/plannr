package de.chennemann.plannr.server.pockets.api.dto

data class UpdatePocketCommand(
    val id: Long,
    val accountId: Long,
    val name: String,
    val description: String?,
    val color: Int,
    val isDefault: Boolean,
)
