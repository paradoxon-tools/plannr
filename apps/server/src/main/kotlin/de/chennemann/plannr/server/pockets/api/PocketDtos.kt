package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.usecases.CreatePocket
import de.chennemann.plannr.server.pockets.usecases.UpdatePocket

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

data class PocketResponse(
    val id: String,
    val accountId: String,
    val name: String,
    val description: String?,
    val color: Int,
    val isDefault: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
) {
    companion object {
        fun from(pocket: Pocket): PocketResponse =
            PocketResponse(
                id = pocket.id,
                accountId = pocket.accountId,
                name = pocket.name,
                description = pocket.description,
                color = pocket.color,
                isDefault = pocket.isDefault,
                isArchived = pocket.isArchived,
                createdAt = pocket.createdAt,
            )
    }
}
