package de.chennemann.plannr.server.contracts.persistence

import de.chennemann.plannr.server.contracts.domain.Contract
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("contracts")
data class ContractModel(
    @Id
    val id: Long?,
    @Column("account_id")
    val accountId: Long,
    @Column("pocket_id")
    val pocketId: Long,
    @Column("partner_id")
    val partnerId: Long?,
    val name: String,
    @Column("start_date")
    val startDate: String,
    @Column("end_date")
    val endDate: String?,
    val notes: String?,
    @Column("is_archived")
    val isArchived: Boolean,
    @Column("created_at")
    val createdAt: Long,
)

fun Contract.toModel(): ContractModel =
    ContractModel(
        id = id,
        accountId = accountId,
        pocketId = pocketId,
        partnerId = partnerId,
        name = name,
        startDate = startDate,
        endDate = endDate,
        notes = notes,
        isArchived = isArchived,
        createdAt = createdAt,
    )

fun ContractModel.toDomain(): Contract =
    Contract(
        id = requireNotNull(id),
        accountId = accountId,
        pocketId = pocketId,
        partnerId = partnerId,
        name = name,
        startDate = startDate,
        endDate = endDate,
        notes = notes,
        isArchived = isArchived,
        createdAt = createdAt,
    )
