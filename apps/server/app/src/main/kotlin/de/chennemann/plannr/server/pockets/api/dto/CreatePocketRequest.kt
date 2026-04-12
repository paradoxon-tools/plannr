package de.chennemann.plannr.server.pockets.api.dto

import de.chennemann.plannr.server.pockets.usecases.CreatePocket

data class CreatePocketRequest(
    val accountId: String,
    val name: String,
    val description: String?,
    val color: Int,
    val isDefault: Boolean,
) {
    fun toCommand(): CreatePocket.Command =
        CreatePocket.Command(
            accountId = accountId,
            name = name,
            description = description,
            color = color,
            isDefault = isDefault,
        )
}
