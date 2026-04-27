package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.api.dto.CreatePocketRequest
import de.chennemann.plannr.server.pockets.api.dto.PocketResponse
import de.chennemann.plannr.server.pockets.api.dto.UpdatePocketRequest
import de.chennemann.plannr.server.pockets.domain.Pocket
import de.chennemann.plannr.server.pockets.service.CreatePocketCommand
import de.chennemann.plannr.server.pockets.service.UpdatePocketCommand

fun CreatePocketRequest.toCommand(): CreatePocketCommand =
    CreatePocketCommand(
        accountId = accountId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
    )

fun UpdatePocketRequest.toCommand(id: String): UpdatePocketCommand =
    UpdatePocketCommand(
        id = id,
        accountId = accountId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
    )

fun Pocket.toResponse(): PocketResponse =
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
