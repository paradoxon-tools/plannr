package de.chennemann.plannr.server.query.transactions.api.dto

import de.chennemann.plannr.server.query.transactions.domain.AccountTransactionFeedItem

data class AccountTransactionFeedItemResponse(
    val accountId: String,
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
    val sourcePocketId: String?,
    val sourcePocketName: String?,
    val sourcePocketColor: Int?,
    val destinationPocketId: String?,
    val destinationPocketName: String?,
    val destinationPocketColor: Int?,
    val isArchived: Boolean,
) {
    companion object {
        fun from(item: AccountTransactionFeedItem): AccountTransactionFeedItemResponse = AccountTransactionFeedItemResponse(
            accountId = item.accountId,
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
            sourcePocketId = item.sourcePocketId,
            sourcePocketName = item.sourcePocketName,
            sourcePocketColor = item.sourcePocketColor,
            destinationPocketId = item.destinationPocketId,
            destinationPocketName = item.destinationPocketName,
            destinationPocketColor = item.destinationPocketColor,
            isArchived = item.isArchived,
        )
    }
}
