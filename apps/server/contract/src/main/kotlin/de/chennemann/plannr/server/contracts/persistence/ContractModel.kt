package de.chennemann.plannr.server.contracts.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("contracts")
data class ContractModel(
    @Id
    @Column("pocket_id")
    val pocketId: Long,
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
