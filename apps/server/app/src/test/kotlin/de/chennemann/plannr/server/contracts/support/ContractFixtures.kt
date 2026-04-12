package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.contracts.dto.CreateContractRequest
import de.chennemann.plannr.server.contracts.dto.UpdateContractRequest
import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.usecases.CreateContract
import de.chennemann.plannr.server.contracts.usecases.UpdateContract

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
    ): CreateContract.Command =
        CreateContract.Command(
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
    ): UpdateContract.Command =
        UpdateContract.Command(
            id = id,
            pocketId = pocketId,
            partnerId = partnerId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
        )

    fun createContractRequest(
        pocketId: String = DEFAULT_POCKET_ID,
        partnerId: String? = DEFAULT_PARTNER_ID,
        name: String = DEFAULT_NAME,
        startDate: String = DEFAULT_START_DATE,
        endDate: String? = DEFAULT_END_DATE,
        notes: String? = DEFAULT_NOTES,
    ): CreateContractRequest =
        CreateContractRequest(
            pocketId = pocketId,
            partnerId = partnerId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
        )

    fun updateContractRequest(
        pocketId: String = DEFAULT_POCKET_ID,
        partnerId: String? = DEFAULT_PARTNER_ID,
        name: String = DEFAULT_NAME,
        startDate: String = DEFAULT_START_DATE,
        endDate: String? = DEFAULT_END_DATE,
        notes: String? = DEFAULT_NOTES,
    ): UpdateContractRequest =
        UpdateContractRequest(
            pocketId = pocketId,
            partnerId = partnerId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
        )
}
