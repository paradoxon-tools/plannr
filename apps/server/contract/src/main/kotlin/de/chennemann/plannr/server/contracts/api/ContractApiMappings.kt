package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.api.dto.ContractResponse
import de.chennemann.plannr.server.contracts.api.dto.CreateContractRequest
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractRequest
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.service.ContractService

internal fun CreateContractRequest.toCreateCommand(): ContractService.CreateCommand =
    ContractService.CreateCommand(
        pocketId = pocketId,
        partnerId = partnerId,
        name = name,
        startDate = startDate,
        endDate = endDate,
        notes = notes,
    )

internal fun UpdateContractRequest.toUpdateCommand(id: String): ContractService.UpdateCommand =
    ContractService.UpdateCommand(
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

