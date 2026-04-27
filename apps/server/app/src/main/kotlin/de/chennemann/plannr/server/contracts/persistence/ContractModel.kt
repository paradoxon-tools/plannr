package de.chennemann.plannr.server.contracts.persistence

import de.chennemann.plannr.server.contracts.domain.Contract

data class ContractModel(
    val id: String?,
    val pocketId: String,
    val partnerId: String?,
    val name: String,
    val startDate: String,
    val endDate: String?,
    val notes: String?,
    val isArchived: Boolean,
    val createdAt: Long,
)

internal fun Contract.toModel(): ContractModel =
    ContractModel(
        id = id,
        pocketId = pocketId,
        partnerId = partnerId,
        name = name,
        startDate = startDate,
        endDate = endDate,
        notes = notes,
        isArchived = isArchived,
        createdAt = createdAt,
    )
