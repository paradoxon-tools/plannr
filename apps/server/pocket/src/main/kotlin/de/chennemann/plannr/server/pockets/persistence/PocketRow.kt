package de.chennemann.plannr.server.pockets.persistence

import de.chennemann.plannr.server.pockets.api.dto.Pocket
import org.springframework.data.relational.core.mapping.Column

data class PocketRow(
    val id: Long,
    @Column("account_id")
    val accountId: Long,
    @Column("contract_id")
    val contractId: Long?,
    val name: String,
    val description: String?,
    val color: Int,
    @Column("is_default")
    val isDefault: Boolean,
    @Column("is_archived")
    val isArchived: Boolean,
    @Column("created_at")
    val createdAt: Long,
)

fun PocketRow.toDTO(): Pocket =
    Pocket(
        id = id,
        accountId = accountId,
        contractId = contractId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
        isArchived = isArchived,
        createdAt = createdAt,
    )
