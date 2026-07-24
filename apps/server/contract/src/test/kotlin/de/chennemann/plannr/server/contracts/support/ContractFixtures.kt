package de.chennemann.plannr.server.contracts.support

import de.chennemann.plannr.server.contracts.api.dto.CreateContractCommand
import de.chennemann.plannr.server.contracts.api.dto.CreateContractPocketCommand
import de.chennemann.plannr.server.contracts.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.contracts.persistence.ContractModel

object ContractFixtures {
    const val DEFAULT_ACCOUNT_ID = 1L
    const val DEFAULT_POCKET_ID = 1L
    const val DEFAULT_PARTNER_ID = 1L
    const val DEFAULT_FINANCIAL_PROFILE_ID = 1L
    const val DEFAULT_SIGNING_DATE = "2024-01-01"
    const val DEFAULT_EXPIRATION_DATE = "2024-12-31"

    fun contractModel(
        pocketId: Long = DEFAULT_POCKET_ID,
        financialProfileId: Long = DEFAULT_FINANCIAL_PROFILE_ID,
        partnerId: Long? = DEFAULT_PARTNER_ID,
        signingDate: String? = DEFAULT_SIGNING_DATE,
        expirationDate: String? = DEFAULT_EXPIRATION_DATE,
        lastCancellationDate: String? = null,
    ): ContractModel =
        ContractModel(
            pocketId = pocketId,
            financialProfileId = financialProfileId,
            partnerId = partnerId,
            signingDate = signingDate,
            expirationDate = expirationDate,
            lastCancellationDate = lastCancellationDate,
        )

    fun createContractCommand(
        financialProfileId: Long? = DEFAULT_FINANCIAL_PROFILE_ID,
        partnerId: Long? = DEFAULT_PARTNER_ID,
        signingDate: String? = DEFAULT_SIGNING_DATE,
        expirationDate: String? = DEFAULT_EXPIRATION_DATE,
        lastCancellationDate: String? = null,
        useDefaultPocket: Boolean = false,
    ): CreateContractCommand =
        CreateContractCommand(
            accountId = DEFAULT_ACCOUNT_ID,
            name = "Bills",
            pocket = CreateContractPocketCommand(
                description = "Monthly fixed costs",
                color = 123456,
                useDefaultPocket = useDefaultPocket,
            ),
            financialProfileId = financialProfileId,
            partnerId = partnerId,
            signingDate = signingDate,
            expirationDate = expirationDate,
            lastCancellationDate = lastCancellationDate,
        )

    fun updateContractCommand(
        id: Long = DEFAULT_POCKET_ID,
        financialProfileId: Long = DEFAULT_FINANCIAL_PROFILE_ID,
        partnerId: Long? = DEFAULT_PARTNER_ID,
        signingDate: String? = DEFAULT_SIGNING_DATE,
        expirationDate: String? = DEFAULT_EXPIRATION_DATE,
        lastCancellationDate: String? = null,
    ): UpdateContractCommand =
        UpdateContractCommand(
            id = id,
            financialProfileId = financialProfileId,
            partnerId = partnerId,
            signingDate = signingDate,
            expirationDate = expirationDate,
            lastCancellationDate = lastCancellationDate,
        )
}
