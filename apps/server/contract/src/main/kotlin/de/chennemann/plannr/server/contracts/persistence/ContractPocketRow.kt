package de.chennemann.plannr.server.contracts.persistence

import de.chennemann.plannr.server.contracts.api.dto.Contract
import org.springframework.data.relational.core.mapping.Column

data class ContractPocketRow(
    val id: Long,
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
    @Column("financial_profile_id")
    val financialProfileId: Long,
    @Column("partner_id")
    val partnerId: Long?,
    @Column("signing_date")
    val signingDate: String?,
    @Column("expiration_date")
    val expirationDate: String?,
    @Column("last_cancellation_date")
    val lastCancellationDate: String?,
)

fun ContractPocketRow.toDTO(): Contract =
    Contract(
        id = id,
        pocketId = id,
        financialProfileId = financialProfileId,
        partnerId = partnerId,
        signingDate = signingDate,
        expirationDate = expirationDate,
        lastCancellationDate = lastCancellationDate,
    )
