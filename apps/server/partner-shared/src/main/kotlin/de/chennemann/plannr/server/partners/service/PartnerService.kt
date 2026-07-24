package de.chennemann.plannr.server.partners.service

import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand

interface PartnerService {
    suspend fun create(command: CreatePartnerCommand): Partner
    suspend fun update(command: UpdatePartnerCommand): Partner
    suspend fun archive(id: Long): Partner
    suspend fun unarchive(id: Long): Partner
    suspend fun delete(id: Long)
    suspend fun list(query: String? = null, archived: Boolean = false): List<Partner>
    suspend fun getById(id: Long): Partner?
}
