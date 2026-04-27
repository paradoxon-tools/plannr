package de.chennemann.plannr.server.partners.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.partners.events.PartnerCreated
import de.chennemann.plannr.server.partners.events.PartnerUpdated
import de.chennemann.plannr.server.partners.persistence.PartnerModel
import de.chennemann.plannr.server.partners.persistence.toModel
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
internal class PartnerServiceImpl(
    private val partnerRepository: PartnerRepository,
    private val timeProvider: TimeProvider,
    private val applicationEventBus: ApplicationEventBus = NoOpApplicationEventBus,
) : PartnerService {
    override suspend fun create(command: CreatePartnerCommand): Partner {
        val created = partnerRepository.save(
            PartnerModel(
                id = null,
                name = command.name,
                notes = command.notes,
                isArchived = false,
                createdAt = timeProvider(),
            ),
        )
        applicationEventBus.publish(PartnerCreated(created))
        return created
    }

    override suspend fun update(command: UpdatePartnerCommand): Partner {
        val existing = existingPartner(command.id)
        val persisted = partnerRepository.update(
            Partner(
                id = existing.id,
                name = command.name,
                notes = command.notes,
                isArchived = existing.isArchived,
                createdAt = existing.createdAt,
            ).toModel(),
        )
        applicationEventBus.publish(PartnerUpdated(existing, persisted))
        return persisted
    }

    override suspend fun archive(id: String): Partner {
        val existing = existingPartner(id)
        val updated = partnerRepository.update(existing.archive().toModel())
        applicationEventBus.publish(PartnerUpdated(existing, updated))
        return updated
    }

    override suspend fun unarchive(id: String): Partner {
        val existing = existingPartner(id)
        val updated = partnerRepository.update(existing.unarchive().toModel())
        applicationEventBus.publish(PartnerUpdated(existing, updated))
        return updated
    }

    override suspend fun list(query: String?, archived: Boolean): List<Partner> =
        partnerRepository.findAll(query?.trim()?.takeIf { it.isNotBlank() }, archived)

    override suspend fun getById(id: String): Partner? =
        partnerRepository.findById(id.trim())

    private suspend fun existingPartner(id: String): Partner =
        partnerRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Partner not found",
                details = mapOf("id" to id.trim()),
            )
}
