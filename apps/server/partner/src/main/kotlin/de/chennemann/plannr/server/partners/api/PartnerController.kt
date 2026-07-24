package de.chennemann.plannr.server.partners.api

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand
import de.chennemann.plannr.server.partners.service.PartnerService
import org.springframework.web.bind.annotation.RestController

@RestController
class PartnerController(
    private val partnerService: PartnerService,
) : PartnerApi {
    override suspend fun create(command: CreatePartnerCommand): Partner =
        partnerService.create(command)

    override suspend fun update(command: UpdatePartnerCommand): Partner =
        partnerService.update(command)

    override suspend fun archive(id: Long): Partner =
        partnerService.archive(id)

    override suspend fun unarchive(id: Long): Partner =
        partnerService.unarchive(id)

    override suspend fun delete(id: Long) =
        partnerService.delete(id)

    override suspend fun list(query: String?, archived: Boolean): List<Partner> =
        partnerService.list(query, archived)

    override suspend fun getById(id: Long): Partner =
        partnerService.getById(id)
            ?: throw NotFoundException("not_found", "Partner not found", mapOf("id" to id))
}
