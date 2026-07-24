package de.chennemann.plannr.server.financialprofiles.api.dto

data class UpdateFinancialProfileCommand(
    val id: Long,
    val name: String,
    val description: String?,
    val kind: String,
)
