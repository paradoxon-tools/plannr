package de.chennemann.plannr.server.partners.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.partners.api.dto.CreatePartnerCommand
import de.chennemann.plannr.server.partners.api.dto.UpdatePartnerCommand
import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.partners.persistence.toDomain
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class PartnerServiceImpl(
    private val partnerRepository: PartnerRepository,
    private val timeProvider: TimeProvider,
) : PartnerService {
    override suspend fun create(command: CreatePartnerCommand): Partner {
        val created = partnerRepository.insert(
            id = null,
            name = command.name,
            notes = command.notes,
            isArchived = false,
            createdAt = timeProvider(),
        ).toDomain()
        return created
    }

    override suspend fun update(command: UpdatePartnerCommand): Partner {
        val existing = existingPartner(command.id)
        val persisted = partnerRepository.update(
            id = existing.id,
            name = command.name,
            notes = command.notes,
            isArchived = existing.isArchived,
        ).toDomain()
        return persisted
    }

    override suspend fun archive(id: String): Partner {
        val existing = existingPartner(id)
        val updated = partnerRepository.update(
            id = existing.id,
            name = existing.name,
            notes = existing.notes,
            isArchived = true,
        ).toDomain()
        return updated
    }

    override suspend fun unarchive(id: String): Partner {
        val existing = existingPartner(id)
        val updated = partnerRepository.update(
            id = existing.id,
            name = existing.name,
            notes = existing.notes,
            isArchived = false,
        ).toDomain()
        return updated
    }

    override suspend fun delete(id: String) {
        val normalizedId = existingPartner(id).id
        partnerRepository.deleteById(normalizedId)
    }

    override suspend fun list(query: String?, archived: Boolean): List<Partner> =
        partnerRepository.findAllByQueryAndArchived(query?.trim()?.takeIf { it.isNotBlank() }, archived)
            .toList()
            .map { it.toDomain() }

    override suspend fun getById(id: String): Partner? =
        partnerRepository.findById(id.trim())?.toDomain()

    private suspend fun existingPartner(id: String): Partner =
        partnerRepository.findById(id.trim())?.toDomain()
            ?: throw NotFoundException(
                code = "not_found",
                message = "Partner not found",
                details = mapOf("id" to id.trim()),
            )
}
