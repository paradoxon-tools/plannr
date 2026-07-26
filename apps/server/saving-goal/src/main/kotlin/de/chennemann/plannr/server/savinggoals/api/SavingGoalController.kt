package de.chennemann.plannr.server.savinggoals.api

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.savinggoals.api.dto.CreateSavingGoalCommand
import de.chennemann.plannr.server.savinggoals.api.dto.SavingGoal
import de.chennemann.plannr.server.savinggoals.api.dto.UpdateSavingGoalCommand
import de.chennemann.plannr.server.savinggoals.service.SavingGoalService
import org.springframework.web.bind.annotation.RestController

@RestController
class SavingGoalController(
    private val savingGoalService: SavingGoalService,
) : SavingGoalApi {
    override suspend fun create(command: CreateSavingGoalCommand): SavingGoal =
        savingGoalService.create(command)

    override suspend fun update(command: UpdateSavingGoalCommand): SavingGoal =
        savingGoalService.update(command)

    override suspend fun archive(id: Long): SavingGoal =
        savingGoalService.archive(id)

    override suspend fun unarchive(id: Long): SavingGoal =
        savingGoalService.unarchive(id)

    override suspend fun list(accountId: Long?, archived: Boolean): List<SavingGoal> =
        savingGoalService.list(accountId, archived)

    override suspend fun getById(id: Long): SavingGoal =
        savingGoalService.getById(id)
            ?: throw NotFoundException("not_found", "Saving goal not found", mapOf("id" to id))
}
