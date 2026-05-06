package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand

object ContractFixtures {
    const val DEFAULT_ID = 1L
    const val DEFAULT_ACCOUNT_ID = 1L
    const val DEFAULT_POCKET_ID = 1L
    const val DEFAULT_PARTNER_ID = 1L
    const val DEFAULT_NAME = "Internet Contract"
    const val DEFAULT_START_DATE = "2024-01-01"
    const val DEFAULT_END_DATE = "2024-12-31"
    const val DEFAULT_NOTES = "12 month term"
    const val DEFAULT_CREATED_AT = 1_710_000_300L

    fun contract(
        id: Long = DEFAULT_ID,
        accountId: Long = DEFAULT_ACCOUNT_ID,
        pocketId: Long = DEFAULT_POCKET_ID,
        partnerId: Long? = DEFAULT_PARTNER_ID,
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
        pocketId: Long = DEFAULT_POCKET_ID,
        partnerId: Long? = DEFAULT_PARTNER_ID,
        name: String = DEFAULT_NAME,
        startDate: String = DEFAULT_START_DATE,
        endDate: String? = DEFAULT_END_DATE,
        notes: String? = DEFAULT_NOTES,
    ): CreateContractCommand =
        CreateContractCommand(
            partnerId = partnerId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
        )

    fun updateContractCommand(
        id: Long = DEFAULT_ID,
        partnerId: Long? = DEFAULT_PARTNER_ID,
        name: String = DEFAULT_NAME,
        startDate: String = DEFAULT_START_DATE,
        endDate: String? = DEFAULT_END_DATE,
        notes: String? = DEFAULT_NOTES,
    ): UpdateContractCommand =
        UpdateContractCommand(
            id = id,
            partnerId = partnerId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
        )

}

