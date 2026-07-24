package de.chennemann.plannr.server.pockets.persistence

import de.chennemann.plannr.server.pockets.api.dto.Pocket
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("pockets")
data class PocketModel(
    @Id
    val id: Long?,
    @Column("account_id")
    val accountId: Long,
    val name: String,
    val description: String?,
    val color: Int,
    @Column("is_default")
    val isDefault: Boolean,
    @Column("is_contract_pocket")
    val isContractPocket: Boolean,
    @Column("is_archived")
    val isArchived: Boolean,
    @Column("created_at")
    val createdAt: Long,
)

fun PocketModel.toDTO(): Pocket =
    Pocket(
        id = requireNotNull(id) { "PocketModel.id must not be null when mapping to domain" },
        accountId = accountId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
        isContractPocket = isContractPocket,
        isArchived = isArchived,
        createdAt = createdAt,
    )

fun Pocket.toModel(): PocketModel =
    PocketModel(
        id = id,
        accountId = accountId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
        isContractPocket = isContractPocket,
        isArchived = isArchived,
        createdAt = createdAt,
    )
