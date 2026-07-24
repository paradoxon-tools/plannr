package de.chennemann.plannr.server.transactions.projection.service

import de.chennemann.plannr.server.common.error.ValidationException
import de.chennemann.plannr.server.transactions.projection.api.dto.TransactionFeedItem
import de.chennemann.plannr.server.transactions.projection.api.dto.TransactionFeedReference
import de.chennemann.plannr.server.transactions.projection.api.dto.TransactionFeedResponse
import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.stereotype.Component

@Component
internal class TransactionFeedServiceImpl(
    private val databaseClient: DatabaseClient,
) : TransactionFeedService {
    override suspend fun getForAccount(
        id: Long,
        cursor: String?,
        limit: Int,
    ): TransactionFeedResponse =
        feedFor(id, ACCOUNT_FEED_SQL, ACCOUNT_BALANCE_SQL, cursor, limit)

    override suspend fun getForPocket(
        id: Long,
        cursor: String?,
        limit: Int,
    ): TransactionFeedResponse =
        feedFor(id, POCKET_FEED_SQL, POCKET_BALANCE_SQL, cursor, limit)

    override suspend fun getForContract(
        id: Long,
        cursor: String?,
        limit: Int,
    ): TransactionFeedResponse =
        feedFor(id, CONTRACT_FEED_SQL, CONTRACT_BALANCE_SQL, cursor, limit)

    private suspend fun feedFor(
        entityId: Long,
        feedSql: String,
        balanceSql: String,
        cursor: String?,
        limit: Int,
    ): TransactionFeedResponse {
        validateLimit(limit)
        val beforeHistoryPosition = cursor?.let(::decodeCursor)
        var query = databaseClient.sql(feedSql)
            .bind("id", entityId)
            .bind("limit", limit + 1)
        query = if (beforeHistoryPosition == null) {
            query.bindNull("before_history_position", java.lang.Long::class.java)
        } else {
            query.bind("before_history_position", beforeHistoryPosition)
        }
        val transactionsWithLookahead = query
            .map { row, _ -> row.toTransactionFeedItem() }
            .all()
            .asFlow()
            .toList()
        val hasMore = transactionsWithLookahead.size > limit
        val transactions = transactionsWithLookahead.take(limit)
        val currentBalance = databaseClient.sql(balanceSql)
            .bind("id", entityId)
            .map { row, _ -> row.get("current_balance", java.lang.Long::class.java)?.toLong() ?: 0L }
            .awaitOneOrNull() ?: 0L

        return TransactionFeedResponse(
            currentBalance = currentBalance,
            transactions = transactions,
            nextCursor = if (hasMore) transactions.lastOrNull()?.historyPosition?.let(::encodeCursor) else null,
            hasMore = hasMore,
        )
    }

    private fun validateLimit(limit: Int) {
        if (limit !in 1..MAX_PAGE_SIZE) {
            throw ValidationException(
                code = "validation_error",
                message = "Transaction feed page size must be between 1 and $MAX_PAGE_SIZE",
                details = mapOf("field" to "limit"),
            )
        }
    }

    private fun decodeCursor(cursor: String): Long =
        try {
            val decoded = String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8)
            decoded.toLong().also { require(it > 0) }
        } catch (exception: Exception) {
            throw ValidationException(
                code = "validation_error",
                message = "Transaction feed cursor is invalid",
                details = mapOf("field" to "cursor"),
            )
        }

    private fun encodeCursor(historyPosition: Long): String =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(historyPosition.toString().toByteArray(StandardCharsets.UTF_8))

    private fun io.r2dbc.spi.Row.toTransactionFeedItem(): TransactionFeedItem =
        TransactionFeedItem(
            transactionId = requiredLong("transaction_id"),
            transactionTemplateId = requiredLong("transaction_template_id"),
            historyPosition = requiredLong("history_position"),
            transactionDate = requiredString("transaction_date"),
            type = requiredString("type"),
            title = requiredString("title"),
            description = get("description", String::class.java),
            transactionAmount = requiredLong("transaction_amount"),
            signedAmount = requiredLong("signed_amount"),
            balanceAfter = requiredLong("balance_after"),
            partner = reference("partner_id", "partner_name"),
            sourcePocket = reference("source_pocket_id", "source_pocket_name", "source_pocket_color"),
            destinationPocket = reference("destination_pocket_id", "destination_pocket_name", "destination_pocket_color"),
            transferPocket = reference("transfer_pocket_id", "transfer_pocket_name", "transfer_pocket_color"),
            isArchived = get("is_archived", java.lang.Boolean::class.java)?.booleanValue() ?: false,
        )

    private fun io.r2dbc.spi.Row.reference(
        idColumn: String,
        nameColumn: String,
        colorColumn: String? = null,
    ): TransactionFeedReference? {
        val id = get(idColumn, java.lang.Long::class.java)?.toLong() ?: return null
        val name = get(nameColumn, String::class.java) ?: return null
        return TransactionFeedReference(
            id = id,
            name = name,
            color = colorColumn?.let { get(it, java.lang.Integer::class.java)?.toInt() },
        )
    }

    private fun io.r2dbc.spi.Row.requiredLong(column: String): Long =
        requireNotNull(get(column, java.lang.Long::class.java)) { "$column must not be null" }.toLong()

    private fun io.r2dbc.spi.Row.requiredString(column: String): String =
        requireNotNull(get(column, String::class.java)) { "$column must not be null" }

    private companion object {
        const val ACCOUNT_FEED_COLUMNS = """
            transaction_id,
            transaction_template_id,
            history_position,
            transaction_date,
            type,
            title,
            description,
            transaction_amount,
            signed_amount,
            balance_after,
            partner_id,
            partner_name,
            source_pocket_id,
            source_pocket_name,
            source_pocket_color,
            destination_pocket_id,
            destination_pocket_name,
            destination_pocket_color,
            NULL::BIGINT AS transfer_pocket_id,
            NULL::VARCHAR AS transfer_pocket_name,
            NULL::INTEGER AS transfer_pocket_color,
            is_archived
        """

        const val POCKET_FEED_COLUMNS = """
            transaction_id,
            transaction_template_id,
            history_position,
            transaction_date,
            type,
            title,
            description,
            transaction_amount,
            signed_amount,
            balance_after,
            partner_id,
            partner_name,
            NULL::BIGINT AS source_pocket_id,
            NULL::VARCHAR AS source_pocket_name,
            NULL::INTEGER AS source_pocket_color,
            NULL::BIGINT AS destination_pocket_id,
            NULL::VARCHAR AS destination_pocket_name,
            NULL::INTEGER AS destination_pocket_color,
            transfer_pocket_id,
            transfer_pocket_name,
            transfer_pocket_color,
            is_archived
        """

        const val ACCOUNT_FEED_SQL = """
            SELECT $ACCOUNT_FEED_COLUMNS
            FROM account_transaction_feed
            WHERE account_id = :id
              AND (:before_history_position IS NULL OR history_position < :before_history_position)
            ORDER BY history_position DESC
            LIMIT :limit
        """

        const val POCKET_FEED_SQL = """
            SELECT $POCKET_FEED_COLUMNS
            FROM pocket_transaction_feed
            WHERE pocket_id = :id
              AND (:before_history_position IS NULL OR history_position < :before_history_position)
            ORDER BY history_position DESC
            LIMIT :limit
        """

        const val CONTRACT_FEED_SQL = """
            SELECT $POCKET_FEED_COLUMNS
            FROM contract_transaction_feed
            WHERE contract_id = :id
              AND (:before_history_position IS NULL OR history_position < :before_history_position)
            ORDER BY history_position DESC
            LIMIT :limit
        """

        const val ACCOUNT_BALANCE_SQL = """
            SELECT balance_after AS current_balance
            FROM account_transaction_feed
            WHERE account_id = :id
            ORDER BY history_position DESC
            LIMIT 1
        """

        const val POCKET_BALANCE_SQL = """
            SELECT balance_after AS current_balance
            FROM pocket_transaction_feed
            WHERE pocket_id = :id
            ORDER BY history_position DESC
            LIMIT 1
        """

        const val CONTRACT_BALANCE_SQL = """
            SELECT balance_after AS current_balance
            FROM contract_transaction_feed
            WHERE contract_id = :id
            ORDER BY history_position DESC
            LIMIT 1
        """

        const val MAX_PAGE_SIZE = 100
    }
}
