package de.chennemann.plannr.server.partners.domain

import de.chennemann.plannr.server.partners.api.dto.Partner
import de.chennemann.plannr.server.partners.persistence.PartnerModel
import de.chennemann.plannr.server.partners.persistence.toDomain
import de.chennemann.plannr.server.partners.persistence.toModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PartnerRepository : CoroutineCrudRepository<PartnerModel, Long> {
    @Query(
        """
        SELECT id, name, notes, is_archived, created_at
        FROM partners
        WHERE (:query IS NULL OR LOWER(name) LIKE LOWER(CONCAT('%', :query, '%')))
          AND is_archived = :archived
        ORDER BY created_at ASC, id ASC
        """,
    )
    fun findAllByQueryAndArchived(query: String?, archived: Boolean): Flow<PartnerModel>
}

internal suspend fun PartnerRepository.save(partner: Partner): Partner =
    save(partner.toModel()).toDomain()
