package de.chennemann.plannr.server.partners.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import org.springframework.stereotype.Component

interface GetPartner {
    suspend operator fun invoke(id: String): Partner
}

@Component
internal class GetPartnerUseCase(
    private val partnerRepository: PartnerRepository,
) : GetPartner {
    override suspend fun invoke(id: String): Partner =
        partnerRepository.findById(id.trim())
            ?: throw NotFoundException(
                code = "not_found",
                message = "Partner not found",
                details = mapOf("id" to id.trim()),
            )
}
