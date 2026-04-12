package de.chennemann.plannr.server.pockets.api.dto

import de.chennemann.plannr.server.pockets.domain.Pocket

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
