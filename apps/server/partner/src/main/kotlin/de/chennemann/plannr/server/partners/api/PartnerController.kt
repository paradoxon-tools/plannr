package de.chennemann.plannr.server.partners.api

import de.chennemann.plannr.server.partners.api.dto.CreatePartnerRequest
import de.chennemann.plannr.server.partners.api.dto.PartnerResponse
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerRequest
import de.chennemann.plannr.server.partners.service.PartnerService
import org.springframework.web.bind.annotation.RestController

@RestController
class PartnerController(
    private val partnerService: PartnerService,
) : PartnerApi {
    override suspend fun create(request: CreatePartnerRequest): PartnerResponse =
        partnerService.create(request.toCommand()).toResponse()

    override suspend fun update(id: String, request: UpdatePartnerRequest): PartnerResponse =
        partnerService.update(request.toCommand(id)).toResponse()

    override suspend fun archive(id: String): PartnerResponse =
        partnerService.archive(id).toResponse()

    override suspend fun unarchive(id: String): PartnerResponse =
        partnerService.unarchive(id).toResponse()

    override suspend fun list(query: String?, archived: Boolean): List<PartnerResponse> =
        partnerService.list(query, archived).map { it.toResponse() }
}
