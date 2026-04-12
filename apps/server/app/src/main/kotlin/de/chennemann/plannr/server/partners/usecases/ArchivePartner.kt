package de.chennemann.plannr.server.partners.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus
import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.partners.events.PartnerUpdated
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface ArchivePartner {
    suspend operator fun invoke(id: String): Partner
}

@Component
@Transactional
internal class ArchivePartnerUseCase(
    private val partnerRepository: PartnerRepository,
    private val applicationEventBus: ApplicationEventBus = NoOpApplicationEventBus,
) : ArchivePartner {
    override suspend fun invoke(id: String): Partner {
        val existing = partnerRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Partner not found",
                details = mapOf("id" to id.trim()),
            )

        val updated = partnerRepository.update(existing.archive())
        applicationEventBus.publish(PartnerUpdated(existing, updated))
        return updated
    }
}
