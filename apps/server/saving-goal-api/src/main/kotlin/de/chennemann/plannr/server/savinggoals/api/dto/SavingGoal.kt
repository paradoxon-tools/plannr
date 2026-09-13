package de.chennemann.plannr.server.savinggoals.api.dto

data class SavingGoal(
    val id: Long,
    val financialProfileId: Long,
    val name: String,
    val description: String?,
    val color: Int,
    val targetAmount: Long,
    val currentAmount: Long,
    val currencyCode: String,
    val targetDate: String?,
    val accountIds: Set<Long>,
    val isCompleted: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
)
