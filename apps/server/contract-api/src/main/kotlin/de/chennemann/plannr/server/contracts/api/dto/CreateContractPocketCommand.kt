package de.chennemann.plannr.server.contracts.api.dto

data class CreateContractPocketCommand(
    val description: String?,
    val color: Int,
    val useDefaultPocket: Boolean = false,
)
