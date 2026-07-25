package de.chennemann.plannr.server.contracts.api.dto

data class Contract(
    val id: Long,
    val financialProfileId: Long,
    val partnerId: Long?,
    val name: String,
    val description: String?,
    val color: Int,
    val type: ContractType,
    val signingDate: String?,
    val expirationDate: String?,
    val lastCancellationDate: String?,
    val isArchived: Boolean,
    val createdAt: Long,
)
