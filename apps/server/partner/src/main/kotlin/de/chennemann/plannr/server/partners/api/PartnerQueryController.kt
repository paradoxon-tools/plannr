package de.chennemann.plannr.server.partners.api

import de.chennemann.plannr.server.partners.api.dto.PartnerResponse
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.query.partners.api.PartnerQueryApi
import org.springframework.web.bind.annotation.RestController

@RestController
class PartnerQueryController(
    private val partnerService: PartnerService,
) : PartnerQueryApi {
    override suspend fun list(query: String?, archived: Boolean): List<PartnerResponse> =
        partnerService.list(query, archived).map { it.toResponse() }
}
