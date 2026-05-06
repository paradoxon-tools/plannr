package de.chennemann.plannr.server.contracts.api.dto

data class Contract(
    val id: Long,
    val accountId: Long,
    val pocketId: Long,
    val partnerId: Long?,
    val name: String,
    val startDate: String,
    val endDate: String?,
    val notes: String?,
    val isArchived: Boolean,
    val createdAt: Long,
)
