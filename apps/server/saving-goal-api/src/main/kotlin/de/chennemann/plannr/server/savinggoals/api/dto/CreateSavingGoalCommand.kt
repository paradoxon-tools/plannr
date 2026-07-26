package de.chennemann.plannr.server.savinggoals.api.dto

data class CreateSavingGoalCommand(
    val name: String,
    val description: String?,
    val color: Int,
    val targetAmount: Long,
    val currencyCode: String,
    val targetDate: String?,
    val accountIds: Set<Long>,
    val financialProfileId: Long?,
)
