package de.chennemann.plannr.server.partners.usecases

import de.chennemann.plannr.server.common.time.TimeProvider
import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.partners.support.PartnerIdGenerator
import org.springframework.stereotype.Component

interface CreatePartner {
    suspend operator fun invoke(command: Command): Partner

    data class Command(
        val name: String,
        val notes: String?,
    )
}

@Component
internal class CreatePartnerUseCase(
    private val partnerRepository: PartnerRepository,
    private val partnerIdGenerator: PartnerIdGenerator,
    private val timeProvider: TimeProvider,
) : CreatePartner {
    override suspend fun invoke(command: CreatePartner.Command): Partner {
        val partner = Partner(
            id = partnerIdGenerator(),
            name = command.name,
            notes = command.notes,
            isArchived = false,
            createdAt = timeProvider(),
        )

        return partnerRepository.save(partner)
    }
}
