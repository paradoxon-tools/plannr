package de.chennemann.plannr.server.partners.support

import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository

class InMemoryPartnerRepository : PartnerRepository {
    private val partners = linkedMapOf<String, Partner>()

    override suspend fun save(partner: Partner): Partner {
        partners[partner.id] = partner
        return partner
    }

    override suspend fun update(partner: Partner): Partner {
        partners[partner.id] = partner
        return partner
    }

    override suspend fun findById(id: String): Partner? = partners[id]

    override suspend fun findAll(query: String?, archived: Boolean): List<Partner> =
        partners.values.filter { partner ->
            partner.isArchived == archived &&
                (query.isNullOrBlank() || partner.name.lowercase().contains(query.trim().lowercase()))
        }
}
