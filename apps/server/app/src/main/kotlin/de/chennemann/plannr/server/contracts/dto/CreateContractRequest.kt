package de.chennemann.plannr.server.contracts.dto

import de.chennemann.plannr.server.contracts.usecases.CreateContract

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
