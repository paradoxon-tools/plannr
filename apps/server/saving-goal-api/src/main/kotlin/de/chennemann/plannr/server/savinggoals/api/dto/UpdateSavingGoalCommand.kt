package de.chennemann.plannr.server.savinggoals.api.dto

data class UpdateSavingGoalCommand(
    val id: Long,
    val financialProfileId: Long,
    val name: String,
    val description: String?,
    val color: Int,
    val targetAmount: Long,
    val currencyCode: String,
    val targetDate: String?,
)
