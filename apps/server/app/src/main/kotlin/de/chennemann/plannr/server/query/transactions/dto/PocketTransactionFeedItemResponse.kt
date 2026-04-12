package de.chennemann.plannr.server.query.transactions.dto

import de.chennemann.plannr.server.query.transactions.domain.PocketTransactionFeedItem

data class PocketTransactionFeedItemResponse(
    val pocketId: String,
    val accountId: String,
    val contractId: String?,
    val transactionId: String,
    val historyPosition: Long,
    val transactionDate: String,
    val type: String,
    val status: String,
    val description: String,
    val transactionAmount: Long,
    val signedAmount: Long,
    val balanceAfter: Long,
    val partnerId: String?,
    val partnerName: String?,
    val transferPocketId: String?,
    val transferPocketName: String?,
    val transferPocketColor: Int?,
    val isArchived: Boolean,
) {
    companion object {
        fun from(item: PocketTransactionFeedItem): PocketTransactionFeedItemResponse = PocketTransactionFeedItemResponse(
            pocketId = item.pocketId,
            accountId = item.accountId,
            contractId = item.contractId,
            transactionId = item.transactionId,
            historyPosition = item.historyPosition,
            transactionDate = item.transactionDate,
            type = item.type,
            status = item.status,
            description = item.description,
            transactionAmount = item.transactionAmount,
            signedAmount = item.signedAmount,
            balanceAfter = item.balanceAfter,
            partnerId = item.partnerId,
            partnerName = item.partnerName,
            transferPocketId = item.transferPocketId,
            transferPocketName = item.transferPocketName,
            transferPocketColor = item.transferPocketColor,
            isArchived = item.isArchived,
        )
    }
}
