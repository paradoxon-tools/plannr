package de.chennemann.plannr.server.pockets.service

data class CreatePocketForContractCommand(
    val accountId: Long,
    val contractId: Long,
)
