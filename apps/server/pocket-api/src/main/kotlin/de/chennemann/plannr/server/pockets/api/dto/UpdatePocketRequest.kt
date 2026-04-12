package de.chennemann.plannr.server.pockets.api.dto

data class UpdatePocketRequest(
    val accountId: String,
    val name: String,
    val description: String?,
    val color: Int,
    val isDefault: Boolean,
)
