package de.chennemann.plannr.server.contracts.api

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
        ContractResponse.from(createContract(request.toCommand()))

    override suspend fun update(id: String, request: UpdateContractRequest): ContractResponse =
        ContractResponse.from(updateContract(request.toCommand(id)))

    override suspend fun archive(id: String): ContractResponse =
        ContractResponse.from(archiveContract(id))

    override suspend fun unarchive(id: String): ContractResponse =
        ContractResponse.from(unarchiveContract(id))
}
