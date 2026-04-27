package de.chennemann.plannr.server.partners.domain

import de.chennemann.plannr.server.partners.persistence.PartnerModel

interface PartnerRepository {
    suspend fun save(partner: PartnerModel): Partner
    suspend fun update(partner: PartnerModel): Partner
    suspend fun findById(id: String): Partner?
    suspend fun findAll(query: String? = null, archived: Boolean = false): List<Partner>
}
