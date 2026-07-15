package de.chennemann.plannr.server.pockets.api.dto

data class CreateContractCommand(
    val partnerId: Long?,
    val signingDate: String?,
    val expirationDate: String?,
    val lastCancellationDate: String?,
    val useDefaultPocket: Boolean = false,
)
