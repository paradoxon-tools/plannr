package de.chennemann.plannr.server.pockets.persistence

import de.chennemann.plannr.server.pockets.api.dto.ContractInfo
import de.chennemann.plannr.server.pockets.api.dto.PocketWithContract
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("contracts")
data class ContractModel(
    @Id
    @Column("pocket_id")
    val pocketId: Long,
    @Column("partner_id")
    val partnerId: Long?,
    @Column("signing_date")
    val signingDate: String?,
    @Column("expiration_date")
    val expirationDate: String?,
    @Column("last_cancellation_date")
    val lastCancellationDate: String?,
)

data class PocketWithContractModel(
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
    @Column("partner_id")
    val partnerId: Long?,
    @Column("signing_date")
    val signingDate: String?,
    @Column("expiration_date")
    val expirationDate: String?,
    @Column("last_cancellation_date")
    val lastCancellationDate: String?,
)

fun PocketWithContractModel.toDto(): PocketWithContract =
    PocketWithContract(
        id = id,
        accountId = accountId,
        name = name,
        description = description,
        color = color,
        isDefault = isDefault,
        isContractPocket = isContractPocket,
        isArchived = isArchived,
        createdAt = createdAt,
        contractInfo = ContractInfo(
            partnerId = partnerId,
            signingDate = signingDate,
            expirationDate = expirationDate,
            lastCancellationDate = lastCancellationDate,
        ),
    )
