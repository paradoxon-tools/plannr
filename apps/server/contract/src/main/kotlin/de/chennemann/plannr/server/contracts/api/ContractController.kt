package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.api.dto.Contract
import de.chennemann.plannr.server.contracts.service.ContractService
import org.springframework.web.bind.annotation.RestController

@RestController
class ContractController(
    private val contractService: ContractService,
) : ContractApi {
    override suspend fun delete(id: Long) =
        contractService.delete(id)

    override suspend fun list(accountId: Long?, archived: Boolean): List<Contract> =
        contractService.list(accountId, archived)
}
