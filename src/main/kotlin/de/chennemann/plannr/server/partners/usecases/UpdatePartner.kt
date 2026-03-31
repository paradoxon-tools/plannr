package de.chennemann.plannr.server.partners.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import org.springframework.stereotype.Component

interface UpdatePartner {
    suspend operator fun invoke(command: Command): Partner

    data class Command(
        val id: String,
        val name: String,
        val notes: String?,
    )
}

@Component
internal class UpdatePartnerUseCase(
    private val partnerRepository: PartnerRepository,
) : UpdatePartner {
    override suspend fun invoke(command: UpdatePartner.Command): Partner {
        val existing = partnerRepository.findById(command.id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Partner not found",
                details = mapOf("id" to command.id.trim()),
            )

        val updated = Partner(
            id = existing.id,
            name = command.name,
            notes = command.notes,
            isArchived = existing.isArchived,
            createdAt = existing.createdAt,
        )

        return partnerRepository.update(updated)
    }
}
