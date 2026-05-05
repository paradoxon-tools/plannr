package de.chennemann.plannr.server.contracts.api.dto

data class CreateContractCommand(
    val pocketId: String,
    val partnerId: String?,
    val name: String,
    val startDate: String,
    val endDate: String?,
    val notes: String?,
)
