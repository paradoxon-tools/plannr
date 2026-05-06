package de.chennemann.plannr.server.pockets.api.dto

data class CreateContractCommand(
    val partnerId: String?,
    val name: String,
    val startDate: String,
    val endDate: String?,
    val notes: String?,
)
