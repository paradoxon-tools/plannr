package de.chennemann.plannr.server.contracts.api.dto

data class ContractResponse(
    val id: String,
    val accountId: String,
    val pocketId: String,
    val partnerId: String?,
    val name: String,
    val startDate: String,
    val endDate: String?,
    val notes: String?,
    val isArchived: Boolean,
    val createdAt: Long,
)
