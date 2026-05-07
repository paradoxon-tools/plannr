package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.service.ContractService
import de.chennemann.plannr.server.pockets.api.dto.PocketWithContract
import org.springframework.web.bind.annotation.RestController

@RestController
class ContractController(
    private val contractService: ContractService,
) : ContractApi {
    override suspend fun delete(id: Long) =
        contractService.delete(id)

    override suspend fun list(accountId: Long?, archived: Boolean): List<PocketWithContract> =
        contractService.list(accountId, archived)
}
