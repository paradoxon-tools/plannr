package de.chennemann.plannr.server.savinggoals.service

import de.chennemann.plannr.server.savinggoals.api.dto.CreateSavingGoalCommand
import de.chennemann.plannr.server.savinggoals.api.dto.SavingGoal
import de.chennemann.plannr.server.savinggoals.api.dto.UpdateSavingGoalCommand

interface SavingGoalService {
    suspend fun create(command: CreateSavingGoalCommand): SavingGoal
    suspend fun update(command: UpdateSavingGoalCommand): SavingGoal
    suspend fun archive(id: Long): SavingGoal
    suspend fun unarchive(id: Long): SavingGoal
    suspend fun list(accountId: Long? = null, archived: Boolean = false): List<SavingGoal>
    suspend fun getById(id: Long): SavingGoal?
}
