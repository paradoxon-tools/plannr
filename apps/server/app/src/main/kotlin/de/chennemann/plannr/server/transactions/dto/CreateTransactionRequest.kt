package de.chennemann.plannr.server.transactions.dto

import de.chennemann.plannr.server.transactions.usecases.CreateTransaction

data class CreateTransactionRequest(
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
) {
    fun toCommand(): CreateTransaction.Command {
        val normalizedType = type.trim().uppercase()
        return CreateTransaction.Command(
            type = type,
            status = status,
            transactionDate = transactionDate,
            amount = amount,
            currencyCode = currencyCode,
            exchangeRate = exchangeRate,
            destinationAmount = destinationAmount,
            description = description,
            partnerId = partnerId,
            sourcePocketId = when (normalizedType) {
                "EXPENSE" -> pocketId ?: sourcePocketId
                else -> sourcePocketId
            },
            destinationPocketId = when (normalizedType) {
                "INCOME" -> pocketId ?: destinationPocketId
                else -> destinationPocketId
            },
        )
    }
}
