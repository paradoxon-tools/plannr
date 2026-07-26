package de.chennemann.plannr.server.savinggoals.domain

import de.chennemann.plannr.server.savinggoals.persistence.SavingGoalModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface SavingGoalRepository : CoroutineCrudRepository<SavingGoalModel, Long> {
    @Query(
        """
        SELECT goal.*
        FROM saving_goals goal
        WHERE (
              :accountId IS NULL
              OR EXISTS (
                  SELECT 1
                  FROM pockets pocket
                  WHERE pocket.saving_goal_id = goal.id
                    AND pocket.account_id = :accountId
              )
          )
          AND goal.is_archived = :archived
        ORDER BY goal.created_at ASC, goal.id ASC
        """,
    )
    fun findAllByAccountIdAndArchived(accountId: Long?, archived: Boolean): Flow<SavingGoalModel>
}
