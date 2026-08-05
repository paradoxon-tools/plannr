package de.chennemann.plannr.server.transactions.templates.api.dto

data class CreateTransactionTemplatesCommand(
    val templates: List<CreateTransactionTemplateWithVersionsCommand>,
)

data class CreateTransactionTemplateWithVersionsCommand(
    val contractId: Long? = null,
    val sourcePocketId: Long?,
    val destinationPocketId: Long?,
    val financialProfileId: Long?,
    val partnerId: Long?,
    val title: String,
    val description: String?,
    val currencyCode: String,
    val transactionType: String,
    val versions: List<CreateTransactionTemplateVersionCommand>,
)
