package de.chennemann.plannr.server.partners.usecases

import de.chennemann.plannr.server.partners.domain.Partner
import de.chennemann.plannr.server.partners.domain.PartnerRepository
import org.springframework.stereotype.Component

interface ListPartnersQuery {
    suspend operator fun invoke(query: String? = null, archived: Boolean = false): List<Partner>
}

@Component
internal class ListPartnersQueryUseCase(
    private val partnerRepository: PartnerRepository,
) : ListPartnersQuery {
    override suspend fun invoke(query: String?, archived: Boolean): List<Partner> =
        partnerRepository.findAll(query?.trim()?.takeIf { it.isNotBlank() }, archived)
}
