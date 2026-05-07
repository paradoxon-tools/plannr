package de.chennemann.plannr.server.pockets.contracts.support

import de.chennemann.plannr.server.pockets.api.dto.CreateContractCommand
import de.chennemann.plannr.server.pockets.api.dto.UpdateContractCommand
import de.chennemann.plannr.server.pockets.persistence.ContractModel

object ContractFixtures {
    const val DEFAULT_ACCOUNT_ID = 1L
    const val DEFAULT_POCKET_ID = 1L
    const val DEFAULT_PARTNER_ID = 1L
    const val DEFAULT_START_DATE = "2024-01-01"
    const val DEFAULT_END_DATE = "2024-12-31"

    fun contractModel(
        pocketId: Long = DEFAULT_POCKET_ID,
        partnerId: Long? = DEFAULT_PARTNER_ID,
        signingDate: String? = DEFAULT_START_DATE,
        expirationDate: String? = DEFAULT_END_DATE,
        lastCancellationDate: String? = null,
    ): ContractModel =
        ContractModel(
            pocketId = pocketId,
            partnerId = partnerId,
            signingDate = signingDate,
            expirationDate = expirationDate,
            lastCancellationDate = lastCancellationDate,
        )

    fun createContractCommand(
        partnerId: Long? = DEFAULT_PARTNER_ID,
        signingDate: String? = DEFAULT_START_DATE,
        expirationDate: String? = DEFAULT_END_DATE,
        lastCancellationDate: String? = null,
    ): CreateContractCommand =
        CreateContractCommand(
            partnerId = partnerId,
            signingDate = signingDate,
            expirationDate = expirationDate,
            lastCancellationDate = lastCancellationDate,
        )

    fun updateContractCommand(
        partnerId: Long? = DEFAULT_PARTNER_ID,
        signingDate: String? = DEFAULT_START_DATE,
        expirationDate: String? = DEFAULT_END_DATE,
        lastCancellationDate: String? = null,
    ): UpdateContractCommand =
        UpdateContractCommand(
            partnerId = partnerId,
            signingDate = signingDate,
            expirationDate = expirationDate,
            lastCancellationDate = lastCancellationDate,
        )
}
