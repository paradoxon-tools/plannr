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
    @Column("contract_id")
    val contractId: Long?,
    val name: String?,
    val description: String?,
    val color: Int?,
    @Column("is_default")
    val isDefault: Boolean,
    @Column("is_archived")
    val isArchived: Boolean,
    @Column("created_at")
    val createdAt: Long,
)

fun Pocket.toModel(): PocketModel =
    PocketModel(
        id = id,
        accountId = accountId,
        contractId = contractId,
        name = if (contractId == null) name else null,
        description = if (contractId == null) description else null,
        color = if (contractId == null) color else null,
        isDefault = isDefault,
        isArchived = isArchived,
        createdAt = createdAt,
    )

/**
 * Converts a pocket read from a resolved query to the API representation.
 *
 * The database check constraint guarantees a direct pocket owns its name and
 * color. For a contract pocket, the repository query resolves both fields
 * from its required contract.
 */
fun PocketModel.toDTO(): Pocket =
    Pocket(
        id = requireNotNull(id),
        accountId = accountId,
        contractId = contractId,
        name = requireNotNull(name),
        description = description,
        color = requireNotNull(color),
        isDefault = isDefault,
        isArchived = isArchived,
        createdAt = createdAt,
    )
