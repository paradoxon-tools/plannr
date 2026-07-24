package de.chennemann.plannr.server.financialprofiles.api.dto

data class CreateFinancialProfileCommand(
    val name: String,
    val description: String?,
    val kind: String,
)
