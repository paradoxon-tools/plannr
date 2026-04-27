package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.api.dto.PocketQueryResponse
import de.chennemann.plannr.server.pockets.domain.PocketQuery

internal fun PocketQuery.toResponse(): PocketQueryResponse =
    PocketQueryResponse(
        id = pocketId,
        accountId = accountId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
        isArchived = isArchived,
        createdAt = createdAt,
        currentBalance = currentBalance,
    )
