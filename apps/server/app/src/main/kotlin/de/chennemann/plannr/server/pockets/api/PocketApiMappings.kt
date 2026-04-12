package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.api.dto.CreatePocketRequest
import de.chennemann.plannr.server.pockets.api.dto.PocketResponse
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketRequest
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.usecases.CreatePocket
import de.chennemann.plannr.server.pockets.usecases.UpdatePocket

internal fun CreatePocketRequest.toCommand(): CreatePocket.Command =
    CreatePocket.Command(
        accountId = accountId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
    )

internal fun UpdatePocketRequest.toCommand(id: String): UpdatePocket.Command =
    UpdatePocket.Command(
        id = id,
        accountId = accountId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
    )

internal fun Pocket.toResponse(): PocketResponse =
    PocketResponse(
        id = id,
        accountId = accountId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
        isArchived = isArchived,
        createdAt = createdAt,
    )
