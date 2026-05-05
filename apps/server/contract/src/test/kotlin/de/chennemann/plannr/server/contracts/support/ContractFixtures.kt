package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.contracts.api.dto.CreateContractCommand
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.contracts.domain.Contract

object ContractFixtures {
    const val DEFAULT_ID = "con_123"
    const val DEFAULT_ACCOUNT_ID = "acc_123"
    const val DEFAULT_POCKET_ID = "poc_123"
    const val DEFAULT_PARTNER_ID = "par_123"
    const val DEFAULT_NAME = "Internet Contract"
    const val DEFAULT_START_DATE = "2024-01-01"
    const val DEFAULT_END_DATE = "2024-12-31"
    const val DEFAULT_NOTES = "12 month term"
    const val DEFAULT_CREATED_AT = 1_710_000_300L

    fun contract(
        id: String = DEFAULT_ID,
        accountId: String = DEFAULT_ACCOUNT_ID,
        pocketId: String = DEFAULT_POCKET_ID,
        partnerId: String? = DEFAULT_PARTNER_ID,
        name: String = DEFAULT_NAME,
        startDate: String = DEFAULT_START_DATE,
        endDate: String? = DEFAULT_END_DATE,
        notes: String? = DEFAULT_NOTES,
        isArchived: Boolean = false,
        createdAt: Long = DEFAULT_CREATED_AT,
    ): Contract =
        Contract(
            id = id,
            accountId = accountId,
            pocketId = pocketId,
            partnerId = partnerId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
            isArchived = isArchived,
            createdAt = createdAt,
        )

    fun createContractCommand(
        pocketId: String = DEFAULT_POCKET_ID,
        partnerId: String? = DEFAULT_PARTNER_ID,
        name: String = DEFAULT_NAME,
        startDate: String = DEFAULT_START_DATE,
        endDate: String? = DEFAULT_END_DATE,
        notes: String? = DEFAULT_NOTES,
    ): CreateContractCommand =
        CreateContractCommand(
            pocketId = pocketId,
            partnerId = partnerId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
        )

    fun updateContractCommand(
        id: String = DEFAULT_ID,
        pocketId: String = DEFAULT_POCKET_ID,
        partnerId: String? = DEFAULT_PARTNER_ID,
        name: String = DEFAULT_NAME,
        startDate: String = DEFAULT_START_DATE,
        endDate: String? = DEFAULT_END_DATE,
        notes: String? = DEFAULT_NOTES,
    ): UpdateContractCommand =
        UpdateContractCommand(
            id = id,
            pocketId = pocketId,
            partnerId = partnerId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
        )

}

