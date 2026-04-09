package de.chennemann.plannr.server.partners.usecases

import de.chennemann.plannr.server.common.events.ApplicationEventBus
import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus
import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.partners.events.PartnerCreated
import de.chennemann.plannr.server.partners.support.PartnerIdGenerator
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface CreatePartner {
    suspend operator fun invoke(command: Command): Partner

    data class Command(
        val name: String,
        val notes: String?,
    )
}

@Component
@Transactional
internal class CreatePartnerUseCase(
    private val partnerRepository: PartnerRepository,
    private val partnerIdGenerator: PartnerIdGenerator,
    private val timeProvider: TimeProvider,
    private val applicationEventBus: ApplicationEventBus = NoOpApplicationEventBus,
) : CreatePartner {
    override suspend fun invoke(command: CreatePartner.Command): Partner {
        val partner = Partner(
            id = partnerIdGenerator(),
            name = command.name,
            notes = command.notes,
            isArchived = false,
            createdAt = timeProvider(),
        )

        val created = partnerRepository.save(partner)
        applicationEventBus.publish(PartnerCreated(created))
        return created
    }
}
