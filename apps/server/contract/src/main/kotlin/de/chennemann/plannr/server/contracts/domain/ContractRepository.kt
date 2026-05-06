package de.chennemann.plannr.server.contracts.domain

import de.chennemann.plannr.server.contracts.persistence.ContractModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ContractRepository : CoroutineCrudRepository<ContractModel, Long> {
    suspend fun findByPocketId(pocketId: Long): ContractModel?

    @Query(
        """
        SELECT id, account_id, pocket_id, partner_id, name, start_date, end_date, notes, is_archived, created_at
        FROM contracts
        WHERE (:accountId IS NULL OR account_id = :accountId)
          AND is_archived = :archived
        ORDER BY created_at ASC, id ASC
        """,
    )
    fun findAllByAccountIdAndArchived(accountId: Long?, archived: Boolean): Flow<ContractModel>
}
