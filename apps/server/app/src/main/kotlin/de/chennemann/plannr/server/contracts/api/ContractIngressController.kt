package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.api.dto.ContractResponse
import de.chennemann.plannr.server.contracts.api.dto.CreateContractRequest
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractRequest
import de.chennemann.plannr.server.contracts.usecases.ArchiveContract
import de.chennemann.plannr.server.contracts.usecases.CreateContract
import de.chennemann.plannr.server.contracts.usecases.UnarchiveContract
import de.chennemann.plannr.server.contracts.usecases.UpdateContract
import org.springframework.web.bind.annotation.RestController

@RestController
class ContractIngressController(
    private val createContract: CreateContract,
    private val updateContract: UpdateContract,
    private val archiveContract: ArchiveContract,
    private val unarchiveContract: UnarchiveContract,
) : ContractIngressApi {
    override suspend fun create(request: CreateContractRequest): ContractResponse =
        createContract(request.toCommand()).toResponse()

    override suspend fun update(id: String, request: UpdateContractRequest): ContractResponse =
        updateContract(request.toCommand(id)).toResponse()

    override suspend fun archive(id: String): ContractResponse =
        archiveContract(id).toResponse()

    override suspend fun unarchive(id: String): ContractResponse =
        unarchiveContract(id).toResponse()
}
