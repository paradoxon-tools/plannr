package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.api.dto.ContractResponse
import de.chennemann.plannr.server.contracts.api.dto.CreateContractRequest
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractRequest
import de.chennemann.plannr.server.contracts.service.ContractService
import org.springframework.web.bind.annotation.RestController

@RestController
class ContractController(
    private val contractService: ContractService,
) : ContractApi {
    override suspend fun create(request: CreateContractRequest): ContractResponse =
        contractService.create(request.toCreateCommand()).toResponse()

    override suspend fun update(id: String, request: UpdateContractRequest): ContractResponse =
        contractService.update(request.toUpdateCommand(id)).toResponse()

    override suspend fun archive(id: String): ContractResponse =
        contractService.archive(id).toResponse()

    override suspend fun unarchive(id: String): ContractResponse =
        contractService.unarchive(id).toResponse()

    override suspend fun list(accountId: String?, archived: Boolean): List<ContractResponse> =
        contractService.list(accountId, archived).map { it.toResponse() }
}

