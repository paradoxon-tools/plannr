package de.chennemann.plannr.server.savinggoals.persistence

import de.chennemann.plannr.server.savinggoals.api.dto.SavingGoal
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("saving_goals")
data class SavingGoalModel(
    @Id
    val id: Long?,
    @Column("financial_profile_id")
    val financialProfileId: Long,
    val name: String,
    val description: String?,
    val color: Int,
    @Column("target_amount")
    val targetAmount: Long,
    @Column("currency_code")
    val currencyCode: String,
    @Column("target_date")
    val targetDate: String?,
    @Column("is_archived")
    val isArchived: Boolean,
    @Column("created_at")
    val createdAt: Long,
)

fun SavingGoalModel.toDTO(
    currentAmount: Long,
    accountIds: Set<Long>,
): SavingGoal =
    SavingGoal(
        id = requireNotNull(id),
        financialProfileId = financialProfileId,
        name = name,
        description = description,
        color = color,
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        currencyCode = currencyCode,
        targetDate = targetDate,
        accountIds = accountIds,
        isCompleted = currentAmount >= targetAmount,
        isArchived = isArchived,
        createdAt = createdAt,
    )
