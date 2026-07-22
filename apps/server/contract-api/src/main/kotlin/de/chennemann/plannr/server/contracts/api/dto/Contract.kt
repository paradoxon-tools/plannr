package de.chennemann.plannr.server.contracts.api.dto

data class Contract(
    val id: Long,
    val pocketId: Long,
    val partnerId: Long?,
    val signingDate: String?,
    val expirationDate: String?,
    val lastCancellationDate: String?,
)
