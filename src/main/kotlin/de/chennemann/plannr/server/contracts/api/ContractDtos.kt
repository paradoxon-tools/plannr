package de.chennemann.plannr.server.contracts.api

import de.chennemann.plannr.server.contracts.domain.Contract
import de.chennemann.plannr.server.contracts.usecases.CreateContract
import de.chennemann.plannr.server.contracts.usecases.UpdateContract

data class CreateContractRequest(
    val pocketId: String,
    val partnerId: String?,
    val name: String,
    val startDate: String,
    val endDate: String?,
    val notes: String?,
) {
    fun toCommand(): CreateContract.Command =
        CreateContract.Command(
            pocketId = pocketId,
            partnerId = partnerId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
        )
}

data class UpdateContractRequest(
    val pocketId: String,
    val partnerId: String?,
    val name: String,
    val startDate: String,
    val endDate: String?,
    val notes: String?,
) {
    fun toCommand(id: String): UpdateContract.Command =
        UpdateContract.Command(
            id = id,
            pocketId = pocketId,
            partnerId = partnerId,
            name = name,
            startDate = startDate,
            endDate = endDate,
            notes = notes,
        )
}

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
) {
    companion object {
        fun from(contract: Contract): ContractResponse =
            ContractResponse(
                id = contract.id,
                accountId = contract.accountId,
                pocketId = contract.pocketId,
                partnerId = contract.partnerId,
                name = contract.name,
                startDate = contract.startDate,
                endDate = contract.endDate,
                notes = contract.notes,
                isArchived = contract.isArchived,
                createdAt = contract.createdAt,
            )
    }
}
