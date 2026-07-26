package de.chennemann.plannr.server.pockets.service

data class CreatePocketForSavingGoalCommand(
    val accountId: Long,
    val savingGoalId: Long,
    val name: String,
    val description: String?,
    val color: Int,
)
