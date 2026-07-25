package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.contracts.api.dto.ContractType
import de.chennemann.plannr.server.contracts.api.dto.CreateContractCommand
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.contracts.persistence.ContractModel

object ContractFixtures {
    const val DEFAULT_ACCOUNT_ID = 1L
    const val DEFAULT_CONTRACT_ID = 1L
    const val DEFAULT_PARTNER_ID = 1L
    const val DEFAULT_FINANCIAL_PROFILE_ID = 1L
    const val DEFAULT_SIGNING_DATE = "2024-01-01"
    const val DEFAULT_EXPIRATION_DATE = "2024-12-31"
    const val DEFAULT_CREATED_AT = 1_710_000_100L

    fun contractModel(
        id: Long? = DEFAULT_CONTRACT_ID,
        financialProfileId: Long = DEFAULT_FINANCIAL_PROFILE_ID,
        partnerId: Long? = DEFAULT_PARTNER_ID,
        type: ContractType = ContractType.ACCUMULATING,
    ) = ContractModel(
        id = id,
        financialProfileId = financialProfileId,
        partnerId = partnerId,
        name = "Bills",
        description = "Monthly fixed costs",
        color = 123456,
        type = type.name,
        signingDate = DEFAULT_SIGNING_DATE,
        expirationDate = DEFAULT_EXPIRATION_DATE,
        lastCancellationDate = null,
        isArchived = false,
        createdAt = DEFAULT_CREATED_AT,
    )

    fun createContractCommand(
        financialProfileId: Long? = DEFAULT_FINANCIAL_PROFILE_ID,
        partnerId: Long? = DEFAULT_PARTNER_ID,
        type: ContractType = ContractType.ACCUMULATING,
        accountIds: Set<Long> = if (type == ContractType.ACCUMULATING) setOf(DEFAULT_ACCOUNT_ID) else emptySet(),
    ) = CreateContractCommand(
        name = "Bills",
        description = "Monthly fixed costs",
        color = 123456,
        type = type,
        accountIds = accountIds,
        financialProfileId = financialProfileId,
        partnerId = partnerId,
        signingDate = DEFAULT_SIGNING_DATE,
        expirationDate = DEFAULT_EXPIRATION_DATE,
        lastCancellationDate = null,
    )

    fun updateContractCommand(
        id: Long = DEFAULT_CONTRACT_ID,
        financialProfileId: Long = DEFAULT_FINANCIAL_PROFILE_ID,
        partnerId: Long? = DEFAULT_PARTNER_ID,
        type: ContractType = ContractType.ACCUMULATING,
        signingDate: String? = DEFAULT_SIGNING_DATE,
        expirationDate: String? = DEFAULT_EXPIRATION_DATE,
    ) = UpdateContractCommand(
        id = id,
        financialProfileId = financialProfileId,
        partnerId = partnerId,
        name = "Bills",
        description = "Monthly fixed costs",
        color = 123456,
        type = type,
        signingDate = signingDate,
        expirationDate = expirationDate,
        lastCancellationDate = null,
    )
}
