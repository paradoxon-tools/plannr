package de.chennemann.plannr.server.contracts.api.dto

data class CreateContractCommand(
    val accountId: Long,
    val name: String,
    val pocket: CreateContractPocketCommand,
    val financialProfileId: Long?,
    val partnerId: Long?,
    val signingDate: String?,
    val expirationDate: String?,
    val lastCancellationDate: String?,
)
