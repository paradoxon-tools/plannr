package de.chennemann.plannr.server.pockets.api.dto

data class UpdateContractCommand(
    val id: Long,
    val partnerId: Long?,
    val name: String,
    val startDate: String,
    val endDate: String?,
    val notes: String?,
)
