package de.chennemann.plannr.server.contracts.api.dto

data class CreateContractCommand(
    val accountId: Long,
    val name: String,
    val pocket: CreateContractPocketCommand,
    val partnerId: Long?,
    val signingDate: String?,
    val expirationDate: String?,
    val lastCancellationDate: String?,
)

data class CreateContractPocketCommand(
    val description: String?,
    val color: Int,
    val useDefaultPocket: Boolean = false,
)
