package de.chennemann.plannr.server.transactions.api.dto

import de.chennemann.plannr.server.transactions.usecases.UpdateTransaction

data class UpdateTransactionRequest(
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
    fun toCommand(id: String): UpdateTransaction.Command {
        val normalizedType = type.trim().uppercase()
        return UpdateTransaction.Command(
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
