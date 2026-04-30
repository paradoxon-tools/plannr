package de.chennemann.plannr.server.contracts.domain

import de.chennemann.plannr.server.contracts.persistence.ContractModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ContractRepository : CoroutineCrudRepository<ContractModel, String> {
    @Query(
        """
        INSERT INTO contracts (id, account_id, pocket_id, partner_id, name, start_date, end_date, notes, is_archived, created_at)
        VALUES (
            COALESCE(:id, CONCAT('con_', REPLACE(gen_random_uuid()::text, '-', ''))),
            :accountId, :pocketId, :partnerId, :name, :startDate, :endDate, :notes, :isArchived, :createdAt
        )
        RETURNING id, account_id, pocket_id, partner_id, name, start_date, end_date, notes, is_archived, created_at
        """,
    )
    suspend fun insert(
        id: String?,
        accountId: String,
        pocketId: String,
        partnerId: String?,
        name: String,
        startDate: String,
        endDate: String?,
        notes: String?,
        isArchived: Boolean,
        createdAt: Long,
    ): ContractModel

    @Query(
        """
        UPDATE contracts
        SET account_id = :accountId,
            pocket_id = :pocketId,
            partner_id = :partnerId,
            name = :name,
            start_date = :startDate,
            end_date = :endDate,
            notes = :notes,
            is_archived = :isArchived
        WHERE id = :id
        RETURNING id, account_id, pocket_id, partner_id, name, start_date, end_date, notes, is_archived, created_at
        """,
    )
    suspend fun update(
        id: String,
        accountId: String,
        pocketId: String,
        partnerId: String?,
        name: String,
        startDate: String,
        endDate: String?,
        notes: String?,
        isArchived: Boolean,
    ): ContractModel

    suspend fun findByPocketId(pocketId: String): ContractModel?

    @Query(
        """
        SELECT id, account_id, pocket_id, partner_id, name, start_date, end_date, notes, is_archived, created_at
        FROM contracts
        WHERE (:accountId IS NULL OR account_id = :accountId)
          AND is_archived = :archived
        ORDER BY created_at ASC, id ASC
        """,
    )
    fun findAllByAccountIdAndArchived(accountId: String?, archived: Boolean): Flow<ContractModel>
}
