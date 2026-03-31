package de.chennemann.plannr.server.partners.usecases

import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import org.springframework.stereotype.Component

interface ListPartners {
    suspend operator fun invoke(query: String? = null, archived: Boolean = false): List<Partner>
}

@Component
internal class ListPartnersUseCase(
    private val partnerRepository: PartnerRepository,
) : ListPartners {
    override suspend fun invoke(query: String?, archived: Boolean): List<Partner> =
        partnerRepository.findAll(query = query?.trim(), archived = archived)
}
