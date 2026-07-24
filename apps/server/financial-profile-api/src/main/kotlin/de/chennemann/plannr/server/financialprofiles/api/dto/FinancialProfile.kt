package de.chennemann.plannr.server.financialprofiles.api.dto

data class FinancialProfile(
    val id: Long,
    val name: String,
    val description: String?,
    val kind: String,
    val isDefault: Boolean,
    val isFallback: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
)
