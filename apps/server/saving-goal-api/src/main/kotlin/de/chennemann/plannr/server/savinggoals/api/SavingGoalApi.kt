package de.chennemann.plannr.server.savinggoals.api

import de.chennemann.plannr.server.savinggoals.api.dto.CreateSavingGoalCommand
import de.chennemann.plannr.server.savinggoals.api.dto.SavingGoal
import de.chennemann.plannr.server.savinggoals.api.dto.UpdateSavingGoalCommand
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

@HttpExchange("/saving-goals")
interface SavingGoalApi {
    @PostExchange
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@RequestBody command: CreateSavingGoalCommand): SavingGoal

    @PutExchange
    suspend fun update(@RequestBody command: UpdateSavingGoalCommand): SavingGoal

    @PostExchange("/{id}/archive")
    suspend fun archive(@PathVariable id: Long): SavingGoal

    @PostExchange("/{id}/unarchive")
    suspend fun unarchive(@PathVariable id: Long): SavingGoal

    @GetExchange
    suspend fun list(
        @RequestParam(required = false) accountId: Long?,
        @RequestParam(defaultValue = "false") archived: Boolean,
    ): List<SavingGoal>

    @GetExchange("/{id}")
    suspend fun getById(@PathVariable id: Long): SavingGoal
}
