package de.chennemann.plannr.server.transactions.domain

data class AccountFutureTransactionFeedItem(
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
    val partnerId: String?,
    val partnerName: String?,
    val sourcePocketId: String?,
    val sourcePocketName: String?,
    val sourcePocketColor: Int?,
    val destinationPocketId: String?,
    val destinationPocketName: String?,
    val destinationPocketColor: Int?,
)

data class PocketFutureTransactionFeedItem(
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
    val partnerId: String?,
    val partnerName: String?,
    val transferPocketId: String?,
    val transferPocketName: String?,
    val transferPocketColor: Int?,
)

interface AccountFutureTransactionFeedRepository {
    suspend fun findPage(accountId: String, fromDate: String?, toDate: String?, after: Long?, limit: Int): List<AccountFutureTransactionFeedItem>
}

interface PocketFutureTransactionFeedRepository {
    suspend fun findPageByPocketId(pocketId: String, fromDate: String?, toDate: String?, after: Long?, limit: Int): List<PocketFutureTransactionFeedItem>
    suspend fun findPageByContractId(contractId: String, fromDate: String?, toDate: String?, after: Long?, limit: Int): List<PocketFutureTransactionFeedItem>
}
