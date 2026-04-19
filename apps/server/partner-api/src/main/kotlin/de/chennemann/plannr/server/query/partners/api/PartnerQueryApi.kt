package de.chennemann.plannr.server.query.partners.api

import de.chennemann.plannr.server.partners.api.dto.PartnerResponse
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange("/query/partners")
interface PartnerQueryApi {
    @GetExchange
    suspend fun list(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<PartnerResponse>
}
