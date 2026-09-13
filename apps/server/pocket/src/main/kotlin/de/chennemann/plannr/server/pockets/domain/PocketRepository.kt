package de.chennemann.plannr.server.pockets.domain

import de.chennemann.plannr.server.pockets.api.dto.Pocket
import de.chennemann.plannr.server.pockets.persistence.PocketModel
import de.chennemann.plannr.server.pockets.persistence.toDTO
import de.chennemann.plannr.server.pockets.persistence.toModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PocketRepository : CoroutineCrudRepository<PocketModel, Long> {
    @Query(
        """
        SELECT p.id, p.account_id, p.contract_id, p.saving_goal_id,
               COALESCE(p.name, c.name) AS name,
               CASE WHEN p.contract_id IS NULL THEN p.description ELSE c.description END AS description,
               COALESCE(p.color, c.color) AS color,
               p.is_default, p.is_archived, p.created_at
        FROM pockets p
        LEFT JOIN contracts c ON c.id = p.contract_id
        WHERE p.account_id = :accountId
          AND p.is_default = TRUE
        ORDER BY p.created_at ASC, p.id ASC
        LIMIT 1
        """,
    )
    suspend fun findDefaultByAccountId(accountId: Long): PocketModel?

    @Query(
        """
        SELECT p.id, p.account_id, p.contract_id, p.saving_goal_id,
               COALESCE(p.name, c.name) AS name,
               CASE WHEN p.contract_id IS NULL THEN p.description ELSE c.description END AS description,
               COALESCE(p.color, c.color) AS color,
               p.is_default, p.is_archived, p.created_at
        FROM pockets p
        LEFT JOIN contracts c ON c.id = p.contract_id
        WHERE (:accountId IS NULL OR p.account_id = :accountId)
          AND (:archived IS NULL OR p.is_archived = :archived)
        ORDER BY p.created_at ASC, p.id ASC
        """,
    )
    fun findAllByAccountIdAndArchived(
        accountId: Long?,
        archived: Boolean?,
    ): Flow<PocketModel>

    @Query(
        """
        SELECT p.id, p.account_id, p.contract_id, p.saving_goal_id,
               COALESCE(p.name, c.name) AS name,
               CASE WHEN p.contract_id IS NULL THEN p.description ELSE c.description END AS description,
               COALESCE(p.color, c.color) AS color,
               p.is_default, p.is_archived, p.created_at
        FROM pockets p
        LEFT JOIN contracts c ON c.id = p.contract_id
        WHERE p.id = :id
        """,
    )
    suspend fun findResolvedById(id: Long): PocketModel?

    @Query(
        """
        SELECT p.id, p.account_id, p.contract_id, p.saving_goal_id,
               COALESCE(p.name, c.name) AS name,
               CASE WHEN p.contract_id IS NULL THEN p.description ELSE c.description END AS description,
               COALESCE(p.color, c.color) AS color,
               p.is_default, p.is_archived, p.created_at
        FROM pockets p
        LEFT JOIN contracts c ON c.id = p.contract_id
        WHERE p.saving_goal_id = :savingGoalId
        ORDER BY p.created_at ASC, p.id ASC
        """,
    )
    fun findAllBySavingGoalId(savingGoalId: Long): Flow<PocketModel>
}

suspend fun PocketRepository.save(pocket: Pocket): Pocket = save(pocket.toModel()).let { findResolvedById(requireNotNull(it.id))!!.toDTO() }
