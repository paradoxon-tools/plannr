package de.chennemann.plannr.server.transactions.api.dto

data class ModifyRecurringOccurrenceRequest(
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
    fun toCommand(transactionId: String): de.chennemann.plannr.server.transactions.usecases.ModifyRecurringOccurrence.Command {
        val normalizedType = type.trim().uppercase()
        return de.chennemann.plannr.server.transactions.usecases.ModifyRecurringOccurrence.Command(
            transactionId = transactionId,
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
