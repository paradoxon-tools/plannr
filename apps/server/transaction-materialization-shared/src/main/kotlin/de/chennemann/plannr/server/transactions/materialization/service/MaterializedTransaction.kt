package de.chennemann.plannr.server.transactions.materialization.service

data class MaterializedTransaction(
    val id: Long,
    val transactionTemplateId: Long,
    val contractId: Long? = null,
    val transactionDate: String,
    val sourcePocketId: Long?,
    val destinationPocketId: Long?,
    val financialProfileId: Long,
    val partnerId: Long?,
    val title: String,
    val description: String?,
    val amount: Long,
    val currencyCode: String,
    val transactionType: String,
    val createdAt: Long,
)
