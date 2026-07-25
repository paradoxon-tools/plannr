package de.chennemann.plannr.server.contracts.api.dto

data class CreateContractCommand(
    val name: String,
    val description: String?,
    val color: Int,
    val type: ContractType,
    val accountIds: Set<Long>,
    val financialProfileId: Long?,
    val partnerId: Long?,
    val signingDate: String?,
    val expirationDate: String?,
    val lastCancellationDate: String?,
)
