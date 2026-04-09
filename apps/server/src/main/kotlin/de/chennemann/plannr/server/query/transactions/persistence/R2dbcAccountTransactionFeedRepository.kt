package de.chennemann.plannr.server.query.transactions.persistence

import de.chennemann.plannr.server.query.transactions.domain.AccountTransactionFeedItem
import de.chennemann.plannr.server.query.transactions.domain.AccountTransactionFeedRepository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class R2dbcAccountTransactionFeedRepository(
    private val databaseClient: DatabaseClient,
) : AccountTransactionFeedRepository {
    override suspend fun findPage(accountId: String, before: Long?, limit: Int): List<AccountTransactionFeedItem> {
        val sql = buildString {
            append(
                """
                SELECT account_id, transaction_id, history_position, transaction_date, type, status, description,
                       transaction_amount, signed_amount, balance_after, partner_id, partner_name,
                       source_pocket_id, source_pocket_name, source_pocket_color,
                       destination_pocket_id, destination_pocket_name, destination_pocket_color, is_archived
                FROM account_transaction_feed
                WHERE account_id = :accountId
                """.trimIndent(),
            )
            if (before != null) {
                append("\n  AND history_position < :before")
            }
            append("\nORDER BY history_position DESC\nLIMIT :limit")
        }

        var spec = databaseClient.sql(sql)
            .bind("accountId", accountId)
            .bind("limit", limit)
        if (before != null) {
            spec = spec.bind("before", before)
        }

        return spec.fetch().all().let { rows ->
            Flux.from(rows)
                .map(::toAccountTransactionFeedItem)
                .collectList()
                .awaitSingle()
        }
    }

    private fun toAccountTransactionFeedItem(row: Map<String, Any?>): AccountTransactionFeedItem = AccountTransactionFeedItem(
        accountId = row.getValue("account_id") as String,
        transactionId = row.getValue("transaction_id") as String,
        historyPosition = (row.getValue("history_position") as Number).toLong(),
        transactionDate = row.getValue("transaction_date") as String,
        type = row.getValue("type") as String,
        status = row.getValue("status") as String,
        description = row.getValue("description") as String,
        transactionAmount = (row.getValue("transaction_amount") as Number).toLong(),
        signedAmount = (row.getValue("signed_amount") as Number).toLong(),
        balanceAfter = (row.getValue("balance_after") as Number).toLong(),
        partnerId = row["partner_id"] as String?,
        partnerName = row["partner_name"] as String?,
        sourcePocketId = row["source_pocket_id"] as String?,
        sourcePocketName = row["source_pocket_name"] as String?,
        sourcePocketColor = (row["source_pocket_color"] as Number?)?.toInt(),
        destinationPocketId = row["destination_pocket_id"] as String?,
        destinationPocketName = row["destination_pocket_name"] as String?,
        destinationPocketColor = (row["destination_pocket_color"] as Number?)?.toInt(),
        isArchived = row.getValue("is_archived") as Boolean,
    )
}
