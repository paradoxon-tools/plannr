package de.chennemann.plannr.server.partners.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import org.springframework.stereotype.Component

interface ArchivePartner {
    suspend operator fun invoke(id: String): Partner
}

@Component
internal class ArchivePartnerUseCase(
    private val partnerRepository: PartnerRepository,
) : ArchivePartner {
    override suspend fun invoke(id: String): Partner {
        val existing = partnerRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Partner not found",
                details = mapOf("id" to id.trim()),
            )

        val updated = existing.archive()
        return partnerRepository.update(updated)
    }
}
