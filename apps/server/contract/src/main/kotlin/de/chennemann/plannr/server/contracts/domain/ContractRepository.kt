package de.chennemann.plannr.server.contracts.domain

import de.chennemann.plannr.server.contracts.persistence.ContractModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ContractRepository : CoroutineCrudRepository<ContractModel, Long> {
    @Query(
        """
        SELECT c.*
        FROM contracts c
        WHERE (
              :accountId IS NULL
              OR EXISTS (
                  SELECT 1 FROM pockets p
                  WHERE p.contract_id = c.id AND p.account_id = :accountId
              )
              OR EXISTS (
                  SELECT 1
                  FROM transaction_templates template
                  LEFT JOIN pockets source ON source.id = template.source_pocket_id
                  LEFT JOIN pockets destination ON destination.id = template.destination_pocket_id
                  WHERE template.contract_id = c.id
                    AND (source.account_id = :accountId OR destination.account_id = :accountId)
              )
          )
          AND c.is_archived = :archived
        ORDER BY c.created_at ASC, c.id ASC
        """,
    )
    fun findAllByAccountIdAndArchived(accountId: Long?, archived: Boolean): Flow<ContractModel>
}
