package de.chennemann.plannr.server.partners.support

import de.chennemann.plannr.server.partners.api.CreatePartnerRequest
import de.chennemann.plannr.server.partners.api.UpdatePartnerRequest
import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.usecases.CreatePartner
import de.chennemann.plannr.server.partners.usecases.UpdatePartner

object PartnerFixtures {
    const val DEFAULT_ID = "par_123"
    const val DEFAULT_NAME = "ACME Corp"
    const val DEFAULT_NOTES = "Preferred partner"
    const val DEFAULT_CREATED_AT = 1_710_000_200L

    fun partner(
        id: String = DEFAULT_ID,
        name: String = DEFAULT_NAME,
        notes: String? = DEFAULT_NOTES,
        isArchived: Boolean = false,
        createdAt: Long = DEFAULT_CREATED_AT,
    ): Partner =
        Partner(
            id = id,
            name = name,
            notes = notes,
            isArchived = isArchived,
            createdAt = createdAt,
        )

    fun createPartnerCommand(
        name: String = DEFAULT_NAME,
        notes: String? = DEFAULT_NOTES,
    ): CreatePartner.Command =
        CreatePartner.Command(
            name = name,
            notes = notes,
        )

    fun updatePartnerCommand(
        id: String = DEFAULT_ID,
        name: String = DEFAULT_NAME,
        notes: String? = DEFAULT_NOTES,
    ): UpdatePartner.Command =
        UpdatePartner.Command(
            id = id,
            name = name,
            notes = notes,
        )

    fun createPartnerRequest(
        name: String = DEFAULT_NAME,
        notes: String? = DEFAULT_NOTES,
    ): CreatePartnerRequest =
        CreatePartnerRequest(
            name = name,
            notes = notes,
        )

    fun updatePartnerRequest(
        name: String = DEFAULT_NAME,
        notes: String? = DEFAULT_NOTES,
    ): UpdatePartnerRequest =
        UpdatePartnerRequest(
            name = name,
            notes = notes,
        )
}
