package de.chennemann.plannr.server.pockets.persistence

import de.chennemann.plannr.server.pockets.domain.Pocket

data class PocketModel(
    val id: String?,
    val accountId: String,
    val name: String,
    val description: String?,
    val color: Int,
    val isDefault: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
)

internal fun PocketModel.toDomain(): Pocket =
    Pocket(
        id = requireNotNull(id) { "PocketModel.id must not be null when mapping to domain" },
        accountId = accountId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
        isArchived = isArchived,
        createdAt = createdAt,
    )

internal fun Pocket.toModel(): PocketModel =
    PocketModel(
        id = id,
        accountId = accountId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
        isArchived = isArchived,
        createdAt = createdAt,
    )
