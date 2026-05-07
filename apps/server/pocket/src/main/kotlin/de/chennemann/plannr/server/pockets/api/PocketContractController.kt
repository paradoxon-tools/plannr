package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.api.dto.PocketWithContract
import de.chennemann.plannr.server.pockets.service.ContractService
import org.springframework.web.bind.annotation.RestController

@RestController
class PocketContractController(
    private val contractService: ContractService,
) : PocketContractApi {
    override suspend fun listContracts(accountId: Long?, archived: Boolean): List<PocketWithContract> =
        contractService.list(accountId, archived)
}
