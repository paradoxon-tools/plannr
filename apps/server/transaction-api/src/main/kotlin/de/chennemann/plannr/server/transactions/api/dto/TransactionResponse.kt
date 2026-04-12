package de.chennemann.plannr.server.transactions.api.dto

data class TransactionResponse(
    val id: String,
    val accountId: String,
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
