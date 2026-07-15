package de.chennemann.plannr.server.pockets.domain

import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.persistence.PocketModel
import de.chennemann.plannr.server.pockets.persistence.toDomain
import de.chennemann.plannr.server.pockets.persistence.toModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PocketRepository : CoroutineCrudRepository<PocketModel, Long> {
    @Query(
        """
        SELECT id, account_id, name, description, color, is_default, is_contract_pocket, is_archived, created_at
        FROM pockets
        WHERE account_id = :accountId
          AND is_default = TRUE
        ORDER BY created_at ASC, id ASC
        LIMIT 1
        """,
    )
    suspend fun findDefaultByAccountId(accountId: Long): PocketModel?

    @Query(
        """
        SELECT id, account_id, name, description, color, is_default, is_contract_pocket, is_archived, created_at
        FROM pockets
        WHERE (:accountId IS NULL OR account_id = :accountId)
          AND (:archived IS NULL OR is_archived = :archived)
        ORDER BY created_at ASC, id ASC
        """,
    )
    fun findAllByAccountIdAndArchived(accountId: Long?, archived: Boolean?): Flow<PocketModel>
}

internal suspend fun PocketRepository.save(pocket: Pocket): Pocket =
    save(pocket.toModel()).toDomain()
