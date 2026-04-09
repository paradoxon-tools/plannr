package de.chennemann.plannr.server.query.transactions.persistence

import de.chennemann.plannr.server.query.transactions.domain.PocketTransactionFeedItem
import de.chennemann.plannr.server.query.transactions.domain.PocketTransactionFeedRepository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class R2dbcPocketTransactionFeedRepository(
    private val databaseClient: DatabaseClient,
) : PocketTransactionFeedRepository {
    override suspend fun findPage(pocketId: String, before: Long?, limit: Int): List<PocketTransactionFeedItem> {
        val sql = buildString {
            append(
                """
                SELECT pocket_id, account_id, transaction_id, history_position, transaction_date, type, status, description,
                       transaction_amount, signed_amount, balance_after, partner_id, partner_name,
                       transfer_pocket_id, transfer_pocket_name, transfer_pocket_color, is_archived
                FROM pocket_transaction_feed
                WHERE pocket_id = :pocketId
                """.trimIndent(),
            )
            if (before != null) {
                append("\n  AND history_position < :before")
            }
            append("\nORDER BY history_position DESC\nLIMIT :limit")
        }

        var spec = databaseClient.sql(sql)
            .bind("pocketId", pocketId)
            .bind("limit", limit)
        if (before != null) {
            spec = spec.bind("before", before)
        }

        return spec.fetch().all().let { rows ->
            Flux.from(rows)
                .map(::toPocketTransactionFeedItem)
                .collectList()
                .awaitSingle()
        }
    }

    private fun toPocketTransactionFeedItem(row: Map<String, Any?>): PocketTransactionFeedItem = PocketTransactionFeedItem(
        pocketId = row.getValue("pocket_id") as String,
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
        transferPocketId = row["transfer_pocket_id"] as String?,
        transferPocketName = row["transfer_pocket_name"] as String?,
        transferPocketColor = (row["transfer_pocket_color"] as Number?)?.toInt(),
        isArchived = row.getValue("is_archived") as Boolean,
    )
}
