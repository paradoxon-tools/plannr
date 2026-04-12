package de.chennemann.plannr.server.query.transactions.api

import de.chennemann.plannr.server.query.transactions.domain.AccountFutureTransactionFeedItem
import de.chennemann.plannr.server.query.transactions.domain.AccountTransactionFeedItem
import de.chennemann.plannr.server.query.transactions.domain.PocketFutureTransactionFeedItem
import de.chennemann.plannr.server.query.transactions.domain.PocketTransactionFeedItem
import de.chennemann.plannr.server.query.transactions.usecases.ListAccountFutureTransactionFeed
import de.chennemann.plannr.server.query.transactions.usecases.ListAccountTransactionFeed
import de.chennemann.plannr.server.query.transactions.usecases.ListPocketFutureTransactionFeed
import de.chennemann.plannr.server.query.transactions.usecases.ListPocketTransactionFeed

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

data class AccountTransactionFeedPageResponse(
    val items: List<AccountTransactionFeedItemResponse>,
    val nextBefore: Long?,
) {
    companion object {
        fun from(page: ListAccountTransactionFeed.Page): AccountTransactionFeedPageResponse = AccountTransactionFeedPageResponse(
            items = page.items.map(AccountTransactionFeedItemResponse::from),
            nextBefore = page.nextBefore,
        )
    }
}

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

data class PocketTransactionFeedPageResponse(
    val items: List<PocketTransactionFeedItemResponse>,
    val nextBefore: Long?,
) {
    companion object {
        fun from(page: ListPocketTransactionFeed.Page): PocketTransactionFeedPageResponse = PocketTransactionFeedPageResponse(
            items = page.items.map(PocketTransactionFeedItemResponse::from),
            nextBefore = page.nextBefore,
        )
    }
}

data class AccountFutureTransactionFeedItemResponse(
    val accountId: String,
    val transactionId: String,
    val futurePosition: Long,
    val transactionDate: String,
    val type: String,
    val status: String,
    val description: String,
    val transactionAmount: Long,
    val signedAmount: Long,
    val projectedBalanceAfter: Long,
) {
    companion object {
        fun from(item: AccountFutureTransactionFeedItem) = AccountFutureTransactionFeedItemResponse(
            accountId = item.accountId,
            transactionId = item.transactionId,
            futurePosition = item.futurePosition,
            transactionDate = item.transactionDate,
            type = item.type,
            status = item.status,
            description = item.description,
            transactionAmount = item.transactionAmount,
            signedAmount = item.signedAmount,
            projectedBalanceAfter = item.projectedBalanceAfter,
        )
    }
}

data class AccountFutureTransactionFeedPageResponse(
    val items: List<AccountFutureTransactionFeedItemResponse>,
    val nextAfter: Long?,
) {
    companion object {
        fun from(page: ListAccountFutureTransactionFeed.Page) = AccountFutureTransactionFeedPageResponse(
            items = page.items.map(AccountFutureTransactionFeedItemResponse::from),
            nextAfter = page.nextAfter,
        )
    }
}

data class PocketFutureTransactionFeedItemResponse(
    val pocketId: String,
    val accountId: String,
    val contractId: String?,
    val transactionId: String,
    val futurePosition: Long,
    val transactionDate: String,
    val type: String,
    val status: String,
    val description: String,
    val transactionAmount: Long,
    val signedAmount: Long,
    val projectedBalanceAfter: Long,
) {
    companion object {
        fun from(item: PocketFutureTransactionFeedItem) = PocketFutureTransactionFeedItemResponse(
            pocketId = item.pocketId,
            accountId = item.accountId,
            contractId = item.contractId,
            transactionId = item.transactionId,
            futurePosition = item.futurePosition,
            transactionDate = item.transactionDate,
            type = item.type,
            status = item.status,
            description = item.description,
            transactionAmount = item.transactionAmount,
            signedAmount = item.signedAmount,
            projectedBalanceAfter = item.projectedBalanceAfter,
        )
    }
}

data class PocketFutureTransactionFeedPageResponse(
    val items: List<PocketFutureTransactionFeedItemResponse>,
    val nextAfter: Long?,
) {
    companion object {
        fun from(page: ListPocketFutureTransactionFeed.Page) = PocketFutureTransactionFeedPageResponse(
            items = page.items.map(PocketFutureTransactionFeedItemResponse::from),
            nextAfter = page.nextAfter,
        )
    }
}
