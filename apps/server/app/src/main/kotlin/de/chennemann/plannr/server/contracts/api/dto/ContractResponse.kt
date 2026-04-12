package de.chennemann.plannr.server.contracts.api.dto

import de.chennemann.plannr.server.contracts.domain.Contract

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
