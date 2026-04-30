package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.api.dto.ContractResponse
import de.chennemann.plannr.server.contracts.api.dto.CreateContractRequest
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractRequest
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.usecases.CreateContract
import de.chennemann.plannr.server.contracts.usecases.UpdateContract

internal fun CreateContractRequest.toCommand(): CreateContract.Command =
    CreateContract.Command(
        pocketId = pocketId,
        partnerId = partnerId,
        name = name,
        startDate = startDate,
        endDate = endDate,
        notes = notes,
    )

internal fun UpdateContractRequest.toCommand(id: String): UpdateContract.Command =
    UpdateContract.Command(
        id = id,
        pocketId = pocketId,
        partnerId = partnerId,
        name = name,
        startDate = startDate,
        endDate = endDate,
        notes = notes,
    )

internal fun Contract.toResponse(): ContractResponse =
    ContractResponse(
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
