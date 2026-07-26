package de.chennemann.plannr.server.savinggoals.support

import de.chennemann.plannr.server.savinggoals.api.dto.CreateSavingGoalCommand
import de.chennemann.plannr.server.savinggoals.api.dto.UpdateSavingGoalCommand
import de.chennemann.plannr.server.savinggoals.persistence.SavingGoalModel

object SavingGoalFixtures {
    const val DEFAULT_ID = 1L
    const val DEFAULT_FINANCIAL_PROFILE_ID = 1L
    const val DEFAULT_ACCOUNT_ID = 1L
    const val DEFAULT_TARGET_AMOUNT = 500_000L
    const val DEFAULT_CREATED_AT = 1_710_000_100L

    fun model(
        id: Long? = DEFAULT_ID,
        financialProfileId: Long = DEFAULT_FINANCIAL_PROFILE_ID,
        isArchived: Boolean = false,
        createdAt: Long = DEFAULT_CREATED_AT,
    ) = SavingGoalModel(
        id = id,
        financialProfileId = financialProfileId,
        name = "Emergency fund",
        description = "Six months of expenses",
        color = 123456,
        targetAmount = DEFAULT_TARGET_AMOUNT,
        currencyCode = "EUR",
        targetDate = "2027-12-31",
        isArchived = isArchived,
        createdAt = createdAt,
    )

    fun createCommand(
        targetAmount: Long = DEFAULT_TARGET_AMOUNT,
        currencyCode: String = "EUR",
        targetDate: String? = "2027-12-31",
        accountIds: Set<Long> = setOf(DEFAULT_ACCOUNT_ID),
        financialProfileId: Long? = DEFAULT_FINANCIAL_PROFILE_ID,
        name: String = "Emergency fund",
    ) = CreateSavingGoalCommand(
        name = name,
        description = "Six months of expenses",
        color = 123456,
        targetAmount = targetAmount,
        currencyCode = currencyCode,
        targetDate = targetDate,
        accountIds = accountIds,
        financialProfileId = financialProfileId,
    )

    fun updateCommand(
        id: Long = DEFAULT_ID,
        targetAmount: Long = 600_000L,
        currencyCode: String = "EUR",
        targetDate: String? = "2028-06-30",
        name: String = "Updated emergency fund",
    ) = UpdateSavingGoalCommand(
        id = id,
        financialProfileId = DEFAULT_FINANCIAL_PROFILE_ID,
        name = name,
        description = "Updated description",
        color = 654321,
        targetAmount = targetAmount,
        currencyCode = currencyCode,
        targetDate = targetDate,
    )
}
