package de.chennemann.plannr.server.partners.api

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
        PartnerResponse.from(createPartner(request.toCommand()))

    override suspend fun update(id: String, request: UpdatePartnerRequest): PartnerResponse =
        PartnerResponse.from(updatePartner(request.toCommand(id)))

    override suspend fun archive(id: String): PartnerResponse =
        PartnerResponse.from(archivePartner(id))

    override suspend fun unarchive(id: String): PartnerResponse =
        PartnerResponse.from(unarchivePartner(id))
}
