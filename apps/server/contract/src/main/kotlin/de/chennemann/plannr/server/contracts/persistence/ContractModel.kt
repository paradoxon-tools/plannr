package de.chennemann.plannr.server.contracts.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import de.chennemann.plannr.server.contracts.api.dto.Contract
import de.chennemann.plannr.server.contracts.api.dto.ContractType

@Table("contracts")
data class ContractModel(
    @Id
    val id: Long?,
    @Column("financial_profile_id")
    val financialProfileId: Long,
    @Column("partner_id")
    val partnerId: Long?,
    val name: String,
    val description: String?,
    val color: Int,
    val type: String,
    @Column("signing_date")
    val signingDate: String?,
    @Column("expiration_date")
    val expirationDate: String?,
    @Column("last_cancellation_date")
    val lastCancellationDate: String?,
    @Column("is_archived")
    val isArchived: Boolean,
    @Column("created_at")
    val createdAt: Long,
)

fun ContractModel.toDTO(): Contract =
    Contract(
        id = requireNotNull(id),
        financialProfileId = financialProfileId,
        partnerId = partnerId,
        name = name,
        description = description,
        color = color,
        type = ContractType.valueOf(type),
        signingDate = signingDate,
        expirationDate = expirationDate,
        lastCancellationDate = lastCancellationDate,
        isArchived = isArchived,
        createdAt = createdAt,
    )
