package de.chennemann.plannr.server.partners.support

import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.partners.persistence.PartnerModel
import de.chennemann.plannr.server.partners.persistence.toDomain

class InMemoryPartnerRepository : PartnerRepository {
    private val partners = linkedMapOf<String, Partner>()

    override suspend fun save(partner: PartnerModel): Partner {
        val persisted = partner.withIdIfMissing("par_${partners.size + 1}").toDomain()
        partners[persisted.id] = persisted
        return persisted
    }

    override suspend fun update(partner: PartnerModel): Partner {
        val persisted = partner.withIdIfMissing("par_${partners.size + 1}").toDomain()
        partners[persisted.id] = persisted
        return persisted
    }

    override suspend fun findById(id: String): Partner? = partners[id]

    override suspend fun findAll(query: String?, archived: Boolean): List<Partner> =
        partners.values.filter { partner ->
            partner.isArchived == archived &&
                (query.isNullOrBlank() || partner.name.lowercase().contains(query.trim().lowercase()))
        }

    private fun PartnerModel.withIdIfMissing(id: String): PartnerModel = copy(id = this.id ?: id)
}
