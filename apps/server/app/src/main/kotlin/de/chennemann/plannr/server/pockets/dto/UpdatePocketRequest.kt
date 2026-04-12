package de.chennemann.plannr.server.pockets.dto

import de.chennemann.plannr.server.pockets.usecases.UpdatePocket

data class UpdatePocketRequest(
    val accountId: String,
    val name: String,
    val description: String?,
    val color: Int,
    val isDefault: Boolean,
) {
    fun toCommand(id: String): UpdatePocket.Command =
        UpdatePocket.Command(
            id = id,
            accountId = accountId,
            name = name,
            description = description,
            color = color,
            isDefault = isDefault,
        )
}
