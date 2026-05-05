package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.api.dto.Contract as ContractDto
import de.chennemann.plannr.server.contracts.domain.Contract

internal fun Contract.toResponse(): ContractDto =
    ContractDto(
        id = id,
        accountId = accountId,
        pocketId = pocketId,
        partnerId = partnerId,
        name = name,
        startDate = startDate,
        endDate = endDate,
        notes = notes,
        isArchived = isArchived,
        createdAt = createdAt,
    )

