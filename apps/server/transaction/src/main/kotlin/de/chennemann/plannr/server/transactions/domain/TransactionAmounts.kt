package de.chennemann.plannr.server.transactions.domain

fun TransactionRecord.accountSignedAmount(): Long = when (type) {
    "EXPENSE" -> -amount
    "INCOME" -> destinationAmount ?: amount
    "TRANSFER" -> 0L
    else -> 0L
}

fun TransactionRecord.pocketSignedAmount(pocketId: String): Long = when {
    sourcePocketId == pocketId -> -amount
    destinationPocketId == pocketId -> destinationAmount ?: amount
    else -> 0L
}

fun TransactionRecord.transferPocketIdFor(pocketId: String): String? = when {
    sourcePocketId == pocketId -> destinationPocketId
    destinationPocketId == pocketId -> sourcePocketId
    else -> null
}
