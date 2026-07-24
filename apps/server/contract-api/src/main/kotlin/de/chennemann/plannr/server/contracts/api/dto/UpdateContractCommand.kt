package de.chennemann.plannr.server.contracts.api.dto

data class UpdateContractCommand(
    val id: Long,
    val partnerId: Long?,
    val signingDate: String?,
    val expirationDate: String?,
    val lastCancellationDate: String?,
)
