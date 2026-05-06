package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.api.dto.Contract
import de.chennemann.plannr.server.contracts.api.dto.CreateContractCommand
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.contracts.service.ContractService
import org.springframework.web.bind.annotation.RestController

@RestController
class ContractController(
    private val contractService: ContractService,
) : ContractApi {
    override suspend fun create(command: CreateContractCommand): Contract =
        contractService.create(command)

    override suspend fun update(command: UpdateContractCommand): Contract =
        contractService.update(command)

    override suspend fun archive(id: String): Contract =
        contractService.archive(id)

    override suspend fun unarchive(id: String): Contract =
        contractService.unarchive(id)

    override suspend fun delete(id: String) =
        contractService.delete(id)

    override suspend fun list(accountId: Long?, archived: Boolean): List<Contract> =
        contractService.list(accountId, archived)
}
