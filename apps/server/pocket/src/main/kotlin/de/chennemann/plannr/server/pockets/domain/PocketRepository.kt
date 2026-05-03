package de.chennemann.plannr.server.pockets.domain

import de.chennemann.plannr.server.pockets.persistence.PocketModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PocketRepository : CoroutineCrudRepository<PocketModel, String> {
    @Query(
        """
        INSERT INTO pockets (id, account_id, name, description, color, is_default, is_archived, created_at)
        VALUES (
            COALESCE(:id, CONCAT('poc_', REPLACE(gen_random_uuid()::text, '-', ''))),
            :accountId, :name, :description, :color, :isDefault, :isArchived, :createdAt
        )
        RETURNING id, account_id, name, description, color, is_default, is_archived, created_at
        """,
    )
    suspend fun insert(
        id: String?,
        accountId: String,
        name: String,
        description: String?,
        color: Int,
        isDefault: Boolean,
        isArchived: Boolean,
        createdAt: Long,
    ): PocketModel

    @Query(
        """
        UPDATE pockets
        SET account_id = :accountId,
            name = :name,
            description = :description,
            color = :color,
            is_default = :isDefault,
            is_archived = :isArchived
        WHERE id = :id
        RETURNING id, account_id, name, description, color, is_default, is_archived, created_at
        """,
    )
    suspend fun update(
        id: String,
        accountId: String,
        name: String,
        description: String?,
        color: Int,
        isDefault: Boolean,
        isArchived: Boolean,
    ): PocketModel

    @Query(
        """
        SELECT id, account_id, name, description, color, is_default, is_archived, created_at
        FROM pockets
        WHERE (:accountId IS NULL OR account_id = :accountId)
          AND (:archived IS NULL OR is_archived = :archived)
        ORDER BY created_at ASC, id ASC
        """,
    )
    fun findAllByAccountIdAndArchived(accountId: String?, archived: Boolean?): Flow<PocketModel>
}
