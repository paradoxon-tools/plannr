package de.chennemann.plannr.server.pockets.service

data class UpdatePocketsForSavingGoalCommand(
    val savingGoalId: Long,
    val name: String,
    val description: String?,
    val color: Int,
)
