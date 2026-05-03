package de.chennemann.plannr.server.partners.domain

import de.chennemann.plannr.server.partners.persistence.PartnerModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface PartnerRepository : CoroutineCrudRepository<PartnerModel, String> {
    @Query(
        """
        INSERT INTO partners (id, name, notes, is_archived, created_at)
        VALUES (
            COALESCE(:id, CONCAT('par_', REPLACE(gen_random_uuid()::text, '-', ''))),
            :name, :notes, :isArchived, :createdAt
        )
        RETURNING id, name, notes, is_archived, created_at
        """,
    )
    suspend fun insert(id: String?, name: String, notes: String?, isArchived: Boolean, createdAt: Long): PartnerModel

    @Query(
        """
        UPDATE partners
        SET name = :name,
            notes = :notes,
            is_archived = :isArchived
        WHERE id = :id
        RETURNING id, name, notes, is_archived, created_at
        """,
    )
    suspend fun update(id: String, name: String, notes: String?, isArchived: Boolean): PartnerModel

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
