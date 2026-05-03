package de.chennemann.plannr.server.transactions.persistence

import de.chennemann.plannr.server.transactions.domain.AccountFutureTransactionFeedItem
import de.chennemann.plannr.server.transactions.domain.AccountFutureTransactionFeedRepository
import de.chennemann.plannr.server.transactions.domain.PocketFutureTransactionFeedItem
import de.chennemann.plannr.server.transactions.domain.PocketFutureTransactionFeedRepository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class R2dbcAccountFutureTransactionFeedRepository(
    private val databaseClient: DatabaseClient,
) : AccountFutureTransactionFeedRepository {
    override suspend fun findPage(accountId: String, fromDate: String?, toDate: String?, after: Long?, limit: Int): List<AccountFutureTransactionFeedItem> {
        val sql = buildString {
            append("SELECT * FROM account_future_transaction_feed WHERE account_id = :scopeId")
            if (fromDate != null) append(" AND transaction_date >= :fromDate")
            if (toDate != null) append(" AND transaction_date <= :toDate")
            if (after != null) append(" AND future_position > :after")
            append(" ORDER BY future_position ASC LIMIT :limit")
        }
        var spec = databaseClient.sql(sql).bind("scopeId", accountId).bind("limit", limit)
        if (fromDate != null) spec = spec.bind("fromDate", fromDate)
        if (toDate != null) spec = spec.bind("toDate", toDate)
        if (after != null) spec = spec.bind("after", after)
        return spec.fetch().all().let { Flux.from(it).map(::toAccountItem).collectList().awaitSingle() }
    }

    private fun toAccountItem(row: Map<String, Any?>) = AccountFutureTransactionFeedItem(
        accountId = row.getValue("account_id") as String,
        transactionId = row.getValue("transaction_id") as String,
        futurePosition = (row.getValue("future_position") as Number).toLong(),
        transactionDate = row.getValue("transaction_date") as String,
        type = row.getValue("type") as String,
        status = row.getValue("status") as String,
        description = row.getValue("description") as String,
        transactionAmount = (row.getValue("transaction_amount") as Number).toLong(),
        signedAmount = (row.getValue("signed_amount") as Number).toLong(),
        projectedBalanceAfter = (row.getValue("projected_balance_after") as Number).toLong(),
        partnerId = row["partner_id"] as String?,
        partnerName = row["partner_name"] as String?,
        sourcePocketId = row["source_pocket_id"] as String?,
        sourcePocketName = row["source_pocket_name"] as String?,
        sourcePocketColor = (row["source_pocket_color"] as Number?)?.toInt(),
        destinationPocketId = row["destination_pocket_id"] as String?,
        destinationPocketName = row["destination_pocket_name"] as String?,
        destinationPocketColor = (row["destination_pocket_color"] as Number?)?.toInt(),
    )
}

@Repository
class R2dbcPocketFutureTransactionFeedRepository(
    private val databaseClient: DatabaseClient,
) : PocketFutureTransactionFeedRepository {
    override suspend fun findPageByPocketId(pocketId: String, fromDate: String?, toDate: String?, after: Long?, limit: Int): List<PocketFutureTransactionFeedItem> =
        findPage("pocket_id = :scopeId", pocketId, fromDate, toDate, after, limit)

    override suspend fun findPageByContractId(contractId: String, fromDate: String?, toDate: String?, after: Long?, limit: Int): List<PocketFutureTransactionFeedItem> =
        findPage("contract_id = :scopeId", contractId, fromDate, toDate, after, limit)

    private suspend fun findPage(scopePredicate: String, scopeId: String, fromDate: String?, toDate: String?, after: Long?, limit: Int): List<PocketFutureTransactionFeedItem> {
        val sql = buildString {
            append("SELECT * FROM pocket_future_transaction_feed WHERE $scopePredicate")
            if (fromDate != null) append(" AND transaction_date >= :fromDate")
            if (toDate != null) append(" AND transaction_date <= :toDate")
            if (after != null) append(" AND future_position > :after")
            append(" ORDER BY future_position ASC LIMIT :limit")
        }
        var spec = databaseClient.sql(sql).bind("scopeId", scopeId).bind("limit", limit)
        if (fromDate != null) spec = spec.bind("fromDate", fromDate)
        if (toDate != null) spec = spec.bind("toDate", toDate)
        if (after != null) spec = spec.bind("after", after)
        return spec.fetch().all().let { Flux.from(it).map(::toPocketItem).collectList().awaitSingle() }
    }

    private fun toPocketItem(row: Map<String, Any?>) = PocketFutureTransactionFeedItem(
        pocketId = row.getValue("pocket_id") as String,
        accountId = row.getValue("account_id") as String,
        contractId = row["contract_id"] as String?,
        transactionId = row.getValue("transaction_id") as String,
        futurePosition = (row.getValue("future_position") as Number).toLong(),
        transactionDate = row.getValue("transaction_date") as String,
        type = row.getValue("type") as String,
        status = row.getValue("status") as String,
        description = row.getValue("description") as String,
        transactionAmount = (row.getValue("transaction_amount") as Number).toLong(),
        signedAmount = (row.getValue("signed_amount") as Number).toLong(),
        projectedBalanceAfter = (row.getValue("projected_balance_after") as Number).toLong(),
        partnerId = row["partner_id"] as String?,
        partnerName = row["partner_name"] as String?,
        transferPocketId = row["transfer_pocket_id"] as String?,
        transferPocketName = row["transfer_pocket_name"] as String?,
        transferPocketColor = (row["transfer_pocket_color"] as Number?)?.toInt(),
    )
}
