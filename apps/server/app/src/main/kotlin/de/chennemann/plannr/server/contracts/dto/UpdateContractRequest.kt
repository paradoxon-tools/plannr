package de.chennemann.plannr.server.contracts.dto

import de.chennemann.plannr.server.contracts.usecases.UpdateContract

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
