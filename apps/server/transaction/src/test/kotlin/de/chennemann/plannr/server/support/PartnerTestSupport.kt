package de.chennemann.plannr.server.support

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand

class FakePartnerService(
    initialPartners: Iterable<Partner> = listOf(Partner(1L, "ACME Corp", "Preferred partner", false, 1_710_000_200L)),
) : PartnerService {
    private val partners = initialPartners.associateByTo(linkedMapOf()) { it.id }

    override suspend fun create(command: CreatePartnerCommand): Partner = throw UnsupportedOperationException("Not used")
    override suspend fun update(command: UpdatePartnerCommand): Partner = throw UnsupportedOperationException("Not used")
    override suspend fun archive(id: Long): Partner = throw UnsupportedOperationException("Not used")
    override suspend fun unarchive(id: Long): Partner = throw UnsupportedOperationException("Not used")
    override suspend fun delete(id: Long) = throw UnsupportedOperationException("Not used")
    override suspend fun list(query: String?, archived: Boolean): List<Partner> = partners.values.toList()
    override suspend fun getById(id: Long): Partner? = partners[id]
}
