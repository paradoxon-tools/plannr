package de.chennemann.plannr.server.partners.domain

interface PartnerRepository {
    suspend fun save(partner: Partner): Partner
    suspend fun update(partner: Partner): Partner
    suspend fun findById(id: String): Partner?
    suspend fun findAll(query: String? = null, archived: Boolean = false): List<Partner>
}
