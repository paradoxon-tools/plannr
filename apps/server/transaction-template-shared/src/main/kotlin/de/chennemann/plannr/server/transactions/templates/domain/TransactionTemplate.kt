package de.chennemann.plannr.server.transactions.templates.domain

data class TransactionTemplate(
    val id: Long,
    val contractId: Long? = null,
    val sourcePocketId: Long?,
    val destinationPocketId: Long?,
    val financialProfileId: Long,
    val partnerId: Long?,
    val title: String,
    val description: String?,
    val currencyCode: String,
    val transactionType: String,
    val versions: List<TransactionTemplateVersion>,
    val isArchived: Boolean,
    val createdAt: Long,
) {
    val currentVersion: TransactionTemplateVersion get() = versions.last()
    val amount get() = currentVersion.amount
    val recurrencePattern get() = currentVersion.recurrencePattern

    constructor(
        id: Long,
        contractId: Long? = null,
        sourcePocketId: Long?,
        destinationPocketId: Long?,
        financialProfileId: Long,
        partnerId: Long?,
        title: String,
        description: String?,
        amount: Long,
        currencyCode: String,
        transactionType: String,
        recurrencePattern: RecurrencePattern,
        previousVersionId: Long?,
        isArchived: Boolean,
        createdAt: Long,
    ) : this(
        id, contractId, sourcePocketId, destinationPocketId, financialProfileId, partnerId,
        title, description, currencyCode, transactionType,
        listOf(TransactionTemplateVersion(id, id, amount, recurrencePattern, recurrencePattern.firstOccurrenceDate, null, createdAt)),
        isArchived, createdAt,
    )
}

data class TransactionTemplateVersion(
    val id: Long,
    val transactionTemplateId: Long,
    val amount: Long,
    val recurrencePattern: RecurrencePattern,
    val validFrom: String,
    val validUntil: String?,
    val createdAt: Long,
)

data class EffectiveTransactionTemplate(
    val template: TransactionTemplate,
    val version: TransactionTemplateVersion,
) {
    val id get() = template.id
    val versionId get() = version.id
    val contractId get() = template.contractId
    val sourcePocketId get() = template.sourcePocketId
    val destinationPocketId get() = template.destinationPocketId
    val financialProfileId get() = template.financialProfileId
    val partnerId get() = template.partnerId
    val title get() = template.title
    val description get() = template.description
    val amount get() = version.amount
    val currencyCode get() = template.currencyCode
    val transactionType get() = template.transactionType
    val recurrencePattern get() = version.recurrencePattern
    val validFrom get() = version.validFrom
    val validUntil get() = version.validUntil
}
