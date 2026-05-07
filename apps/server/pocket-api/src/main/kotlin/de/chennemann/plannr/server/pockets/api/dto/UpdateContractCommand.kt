package de.chennemann.plannr.server.pockets.api.dto

data class UpdateContractCommand(
    val partnerId: Long?,
    val signingDate: String?,
    val expirationDate: String?,
    val lastCancellationDate: String?,
)
