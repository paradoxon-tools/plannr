package de.chennemann.plannr.server.transactions.materialization.persistence

import de.chennemann.plannr.server.transactions.materialization.service.MaterializedTransaction
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("transaction_materializations")
data class MaterializedTransactionModel(
    @Id
    val id: Long?,
    @Column("transaction_template_id")
    val transactionTemplateId: Long,
    @Column("transaction_date")
    val transactionDate: String,
    @Column("source_pocket_id")
    val sourcePocketId: Long?,
    @Column("destination_pocket_id")
    val destinationPocketId: Long?,
    @Column("partner_id")
    val partnerId: Long?,
    val title: String,
    val description: String?,
    val amount: Long,
    @Column("currency_code")
    val currencyCode: String,
    @Column("transaction_type")
    val transactionType: String,
    @Column("created_at")
    val createdAt: Long,
)

fun MaterializedTransactionModel.toDomain(): MaterializedTransaction =
    MaterializedTransaction(
        id = requireNotNull(id) { "MaterializedTransactionModel.id must not be null when mapping to domain" },
        transactionTemplateId = transactionTemplateId,
        transactionDate = transactionDate,
        sourcePocketId = sourcePocketId,
        destinationPocketId = destinationPocketId,
        partnerId = partnerId,
        title = title,
        description = description,
        amount = amount,
        currencyCode = currencyCode,
        transactionType = transactionType,
        createdAt = createdAt,
    )
