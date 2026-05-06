package de.chennemann.plannr.server.pockets.domain

import de.chennemann.plannr.server.pockets.persistence.PocketModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PocketRepository : CoroutineCrudRepository<PocketModel, Long> {
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
