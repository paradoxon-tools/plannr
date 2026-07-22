package de.chennemann.plannr.server.pockets.service

data class CreatePocketForContractCommand(
    val accountId: Long,
    val name: String,
    val description: String?,
    val color: Int,
    val useDefaultPocket: Boolean = false,
)
