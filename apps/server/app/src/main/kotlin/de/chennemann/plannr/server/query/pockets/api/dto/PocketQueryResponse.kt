package de.chennemann.plannr.server.query.pockets.api.dto

import de.chennemann.plannr.server.query.pockets.domain.PocketQuery

data class PocketQueryResponse(
    val id: String,
    val accountId: String,
    val name: String,
    val description: String?,
    val color: Int,
    val isDefault: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
    val currentBalance: Long,
) {
    companion object {
        fun from(pocketQuery: PocketQuery): PocketQueryResponse = PocketQueryResponse(
            id = pocketQuery.pocketId,
            accountId = pocketQuery.accountId,
            name = pocketQuery.name,
            description = pocketQuery.description,
            color = pocketQuery.color,
            isDefault = pocketQuery.isDefault,
            isArchived = pocketQuery.isArchived,
            createdAt = pocketQuery.createdAt,
            currentBalance = pocketQuery.currentBalance,
        )
    }
}
