package de.chennemann.plannr.server.partners.service

import de.chennemann.plannr.server.partners.domain.Partner

interface PartnerService {
    suspend fun create(command: CreatePartnerCommand): Partner

    suspend fun update(command: UpdatePartnerCommand): Partner

    suspend fun archive(id: String): Partner

    suspend fun unarchive(id: String): Partner

    suspend fun list(query: String? = null, archived: Boolean = false): List<Partner>

    suspend fun getById(id: String): Partner?
}

data class CreatePartnerCommand(
    val name: String,
    val notes: String?,
)

data class UpdatePartnerCommand(
    val id: String,
    val name: String,
    val notes: String?,
)
