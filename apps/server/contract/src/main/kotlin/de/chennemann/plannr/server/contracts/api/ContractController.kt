package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.common.error.NotFoundException
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

    override suspend fun update(id: Long, command: UpdateContractCommand): Contract =
        contractService.update(id, command)

    override suspend fun list(accountId: Long?, archived: Boolean): List<Contract> =
        contractService.list(accountId, archived)

    override suspend fun getById(id: Long): Contract =
        contractService.getById(id)
            ?: throw NotFoundException("not_found", "Contract not found", mapOf("id" to id))
}
