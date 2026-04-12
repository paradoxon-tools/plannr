package de.chennemann.plannr.server.partners.api

import de.chennemann.plannr.server.partners.api.dto.CreatePartnerRequest
import de.chennemann.plannr.server.partners.api.dto.PartnerResponse
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerRequest
import de.chennemann.plannr.server.partners.usecases.ArchivePartner
import de.chennemann.plannr.server.partners.usecases.CreatePartner
import de.chennemann.plannr.server.partners.usecases.UnarchivePartner
import de.chennemann.plannr.server.partners.usecases.UpdatePartner
import org.springframework.web.bind.annotation.RestController

@RestController
class PartnerIngressController(
    private val createPartner: CreatePartner,
    private val updatePartner: UpdatePartner,
    private val archivePartner: ArchivePartner,
    private val unarchivePartner: UnarchivePartner,
) : PartnerIngressApi {
    override suspend fun create(request: CreatePartnerRequest): PartnerResponse =
        createPartner(request.toCommand()).toResponse()

    override suspend fun update(id: String, request: UpdatePartnerRequest): PartnerResponse =
        updatePartner(request.toCommand(id)).toResponse()

    override suspend fun archive(id: String): PartnerResponse =
        archivePartner(id).toResponse()

    override suspend fun unarchive(id: String): PartnerResponse =
        unarchivePartner(id).toResponse()
}
