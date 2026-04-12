package de.chennemann.plannr.server.partners.api

import de.chennemann.plannr.server.partners.api.dto.CreatePartnerRequest
import de.chennemann.plannr.server.partners.api.dto.PartnerResponse
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerRequest
import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.usecases.CreatePartner
import de.chennemann.plannr.server.partners.usecases.UpdatePartner

internal fun CreatePartnerRequest.toCommand(): CreatePartner.Command =
    CreatePartner.Command(
        name = name,
        notes = notes,
    )

internal fun UpdatePartnerRequest.toCommand(id: String): UpdatePartner.Command =
    UpdatePartner.Command(
        id = id,
        name = name,
        notes = notes,
    )

internal fun Partner.toResponse(): PartnerResponse =
    PartnerResponse(
        id = id,
        name = name,
        notes = notes,
        isArchived = isArchived,
        createdAt = createdAt,
    )
