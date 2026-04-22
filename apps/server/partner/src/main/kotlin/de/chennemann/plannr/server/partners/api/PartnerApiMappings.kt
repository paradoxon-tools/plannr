package de.chennemann.plannr.server.partners.api

import de.chennemann.plannr.server.partners.api.dto.CreatePartnerRequest
import de.chennemann.plannr.server.partners.api.dto.PartnerResponse
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerRequest
import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.service.CreatePartnerCommand
import de.chennemann.plannr.server.partners.service.UpdatePartnerCommand

internal fun CreatePartnerRequest.toCommand(): CreatePartnerCommand =
    CreatePartnerCommand(
        name = name,
        notes = notes,
    )

internal fun UpdatePartnerRequest.toCommand(id: String): UpdatePartnerCommand =
    UpdatePartnerCommand(
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
