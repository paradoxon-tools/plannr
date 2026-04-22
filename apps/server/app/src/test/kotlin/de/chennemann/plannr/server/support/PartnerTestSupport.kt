package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.service.CreatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.partners.service.UpdatePartnerCommand

object TestPartners {
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
}

class FakePartnerService(
    initialPartners: Iterable<Partner> = listOf(TestPartners.partner()),
    private val idGenerator: () -> String = { "par_new" },
    private val timeProvider: () -> Long = { TestPartners.DEFAULT_CREATED_AT },
) : PartnerService {
    private val partners = initialPartners.associateByTo(linkedMapOf()) { it.id }

    override suspend fun create(command: CreatePartnerCommand): Partner {
        val partner = Partner(
            id = idGenerator(),
            name = command.name,
            notes = command.notes,
            isArchived = false,
            createdAt = timeProvider(),
        )
        partners[partner.id] = partner
        return partner
    }

    override suspend fun update(command: UpdatePartnerCommand): Partner {
        val existing = existingPartner(command.id)
        val partner = Partner(
            id = existing.id,
            name = command.name,
            notes = command.notes,
            isArchived = existing.isArchived,
            createdAt = existing.createdAt,
        )
        partners[partner.id] = partner
        return partner
    }

    override suspend fun archive(id: String): Partner {
        val partner = existingPartner(id).archive()
        partners[partner.id] = partner
        return partner
    }

    override suspend fun unarchive(id: String): Partner {
        val partner = existingPartner(id).unarchive()
        partners[partner.id] = partner
        return partner
    }

    override suspend fun list(query: String?, archived: Boolean): List<Partner> {
        val normalizedQuery = query?.trim()?.takeIf { it.isNotBlank() }
        return partners.values
            .filter { it.isArchived == archived }
            .filter { partner ->
                normalizedQuery == null ||
                    partner.name.contains(normalizedQuery, ignoreCase = true) ||
                    partner.notes?.contains(normalizedQuery, ignoreCase = true) == true
            }
            .sortedBy { it.name }
    }

    override suspend fun getById(id: String): Partner? =
        partners[id.trim()]

    private fun existingPartner(id: String): Partner =
        partners[id.trim()]
            ?: throw NotFoundException("not_found", "Partner not found", mapOf("id" to id.trim()))
}
