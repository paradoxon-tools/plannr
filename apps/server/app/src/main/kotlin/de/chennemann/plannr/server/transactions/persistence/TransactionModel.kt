package de.chennemann.plannr.server.transactions.persistence

import de.chennemann.plannr.server.transactions.domain.TransactionRecord

data class TransactionModel(
    val id: String?,
    val type: String,
    val status: String,
    val transactionDate: String,
    val amount: Long,
    val currencyCode: String,
    val exchangeRate: String?,
    val destinationAmount: Long?,
    val description: String,
    val partnerId: String?,
    val pocketId: String?,
    val sourcePocketId: String?,
    val destinationPocketId: String?,
    val parentTransactionId: String?,
    val recurringTransactionId: String?,
    val modifiedById: String?,
    val transactionOrigin: String,
    val isArchived: Boolean,
    val createdAt: Long,
)

internal fun TransactionModel.persistedPocketId(): String? = if (type == "TRANSFER") null else pocketId

internal fun TransactionModel.persistedSourcePocketId(): String? = if (type == "TRANSFER") sourcePocketId else null

internal fun TransactionModel.persistedDestinationPocketId(): String? = if (type == "TRANSFER") destinationPocketId else null

internal fun TransactionRecord.toModel(): TransactionModel =
    TransactionModel(
        id = id,
        type = type,
        status = status,
        transactionDate = transactionDate,
        amount = amount,
        currencyCode = currencyCode,
        exchangeRate = exchangeRate,
        destinationAmount = destinationAmount,
        description = description,
        partnerId = partnerId,
        pocketId = pocketId,
        sourcePocketId = sourcePocketId,
        destinationPocketId = destinationPocketId,
        parentTransactionId = parentTransactionId,
        recurringTransactionId = recurringTransactionId,
        modifiedById = modifiedById,
        transactionOrigin = transactionOrigin,
        isArchived = isArchived,
        createdAt = createdAt,
    )
