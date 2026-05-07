package de.chennemann.plannr.server.pockets.api

import de.chennemann.plannr.server.pockets.api.dto.PocketWithContract
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange("/contracts")
interface PocketContractApi {
    @GetExchange
    suspend fun listContracts(
        @RequestParam(required = false) accountId: Long?,
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<PocketWithContract>
}
