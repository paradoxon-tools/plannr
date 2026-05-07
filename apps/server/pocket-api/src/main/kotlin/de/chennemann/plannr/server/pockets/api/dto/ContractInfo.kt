package de.chennemann.plannr.server.pockets.api.dto

data class ContractInfo(
    val partnerId: Long?,
    val signingDate: String?,
    val expirationDate: String?,
    val lastCancellationDate: String?,
)
