package de.chennemann.plannr.server.transactions.persistence

import de.chennemann.plannr.server.transactions.domain.TransactionRecord
import de.chennemann.plannr.server.transactions.domain.TransactionRepository
import de.chennemann.plannr.server.transactions.domain.TransactionVisibility
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class R2dbcTransactionRepository(
    private val databaseClient: DatabaseClient,
) : TransactionRepository {
    override suspend fun save(transaction: TransactionRecord): TransactionRecord {
        bindAll(
            databaseClient.sql(
                """
                INSERT INTO transactions (
                    id, pocket_id, type, status, transaction_date, amount, currency_code, exchange_rate,
                    destination_amount, description, partner_id, source_pocket_id, destination_pocket_id,
                    parent_transaction_id, recurring_transaction_id, modified_by_id, transaction_origin, is_archived, created_at
                ) VALUES (
                    :id, :pocketId, :type, :status, :transactionDate, :amount, :currencyCode, :exchangeRate,
                    :destinationAmount, :description, :partnerId, :sourcePocketId, :destinationPocketId,
                    :parentTransactionId, :recurringTransactionId, :modifiedById, :transactionOrigin, :isArchived, :createdAt
                )
                """.trimIndent(),
            ),
            transaction,
        )
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        return transaction
    }

    override suspend fun update(transaction: TransactionRecord): TransactionRecord {
        bindAll(
            databaseClient.sql(
                """
                UPDATE transactions
                SET pocket_id = :pocketId,
                    type = :type,
                    status = :status,
                    transaction_date = :transactionDate,
                    amount = :amount,
                    currency_code = :currencyCode,
                    exchange_rate = :exchangeRate,
                    destination_amount = :destinationAmount,
                    description = :description,
                    partner_id = :partnerId,
                    source_pocket_id = :sourcePocketId,
                    destination_pocket_id = :destinationPocketId,
                    parent_transaction_id = :parentTransactionId,
                    recurring_transaction_id = :recurringTransactionId,
                    modified_by_id = :modifiedById,
                    transaction_origin = :transactionOrigin,
                    is_archived = :isArchived
                WHERE id = :id
                """.trimIndent(),
            ),
            transaction,
        )
            .fetch()
            .rowsUpdated()
            .awaitSingle()
        return transaction
    }

    override suspend fun findById(id: String): TransactionRecord? =
        databaseClient.sql("$baseSelect WHERE t.id = :id")
            .bind("id", id)
            .fetch()
            .one()
            .map(::toTransaction)
            .awaitSingleOrNull()

    override suspend fun findVisibleByAccountId(accountId: String): List<TransactionRecord> =
        findAll(
            """
            WHERE (p.account_id = :scopeId OR sp.account_id = :scopeId OR dp.account_id = :scopeId)
              AND ${TransactionVisibility.SQL_PREDICATE.replace("modified_by_id", "t.modified_by_id").replace("is_archived", "t.is_archived")}
            ORDER BY t.transaction_date ASC, t.created_at ASC, t.id ASC
            """.trimIndent(),
            accountId,
        )

    override suspend fun findVisibleByPocketId(pocketId: String): List<TransactionRecord> =
        findAll(
            """
            WHERE (t.pocket_id = :scopeId OR t.source_pocket_id = :scopeId OR t.destination_pocket_id = :scopeId)
              AND ${TransactionVisibility.SQL_PREDICATE.replace("modified_by_id", "t.modified_by_id").replace("is_archived", "t.is_archived")}
            ORDER BY t.transaction_date ASC, t.created_at ASC, t.id ASC
            """.trimIndent(),
            pocketId,
        )

    override suspend fun findByRecurringTransactionId(recurringTransactionId: String): List<TransactionRecord> =
        findAll(
            """
            WHERE t.recurring_transaction_id = :scopeId
            ORDER BY t.transaction_date ASC, t.created_at ASC, t.id ASC
            """.trimIndent(),
            recurringTransactionId,
        )

    override suspend fun findVisibleByRecurringTransactionId(recurringTransactionId: String): List<TransactionRecord> =
        findAll(
            """
            WHERE t.recurring_transaction_id = :scopeId
              AND ${TransactionVisibility.SQL_PREDICATE.replace("modified_by_id", "t.modified_by_id").replace("is_archived", "t.is_archived")}
            ORDER BY t.transaction_date ASC, t.created_at ASC, t.id ASC
            """.trimIndent(),
            recurringTransactionId,
        )

    override suspend fun findVisiblePending(): List<TransactionRecord> =
        databaseClient.sql(
            """
            $baseSelect
            WHERE ${TransactionVisibility.SQL_PREDICATE.replace("modified_by_id", "t.modified_by_id").replace("is_archived", "t.is_archived")}
              AND t.status = 'PENDING'
            ORDER BY t.transaction_date ASC, t.created_at ASC, t.id ASC
            """.trimIndent(),
        )
            .fetch()
            .all()
            .let { rows -> Flux.from(rows).map(::toTransaction).collectList().awaitSingle() }

    override suspend fun findVisibleFutureByAccountId(accountId: String, startDateInclusive: String, endDateInclusive: String): List<TransactionRecord> =
        findAllByScopeAndDateRange(
            scopePredicate = "(p.account_id = :scopeId OR sp.account_id = :scopeId OR dp.account_id = :scopeId)",
            scopeId = accountId,
            startDateInclusive = startDateInclusive,
            endDateInclusive = endDateInclusive,
        )

    override suspend fun findVisibleFutureByPocketId(pocketId: String, startDateInclusive: String, endDateInclusive: String): List<TransactionRecord> =
        findAllByScopeAndDateRange(
            scopePredicate = "(t.pocket_id = :scopeId OR t.source_pocket_id = :scopeId OR t.destination_pocket_id = :scopeId)",
            scopeId = pocketId,
            startDateInclusive = startDateInclusive,
            endDateInclusive = endDateInclusive,
        )

    private suspend fun findAll(whereClause: String, scopeId: String): List<TransactionRecord> =
        databaseClient.sql("$baseSelect $whereClause")
            .bind("scopeId", scopeId)
            .fetch()
            .all()
            .let { rows -> Flux.from(rows).map(::toTransaction).collectList().awaitSingle() }

    private suspend fun findAllByScopeAndDateRange(
        scopePredicate: String,
        scopeId: String,
        startDateInclusive: String,
        endDateInclusive: String,
    ): List<TransactionRecord> =
        databaseClient.sql(
            """
            $baseSelect
            WHERE $scopePredicate
              AND ${TransactionVisibility.SQL_PREDICATE.replace("modified_by_id", "t.modified_by_id").replace("is_archived", "t.is_archived")}
              AND t.transaction_date BETWEEN :startDateInclusive AND :endDateInclusive
            ORDER BY t.transaction_date ASC, t.created_at ASC, t.id ASC
            """.trimIndent(),
        )
            .bind("scopeId", scopeId)
            .bind("startDateInclusive", startDateInclusive)
            .bind("endDateInclusive", endDateInclusive)
            .fetch()
            .all()
            .let { rows -> Flux.from(rows).map(::toTransaction).collectList().awaitSingle() }

    private fun bindAll(spec: DatabaseClient.GenericExecuteSpec, transaction: TransactionRecord): DatabaseClient.GenericExecuteSpec {
        var current = spec
            .bind("id", transaction.id)
            .bind("type", transaction.type)
            .bind("status", transaction.status)
            .bind("transactionDate", transaction.transactionDate)
            .bind("amount", transaction.amount)
            .bind("currencyCode", transaction.currencyCode)
            .bind("description", transaction.description)
            .bind("transactionOrigin", transaction.transactionOrigin)
            .bind("isArchived", transaction.isArchived)
            .bind("createdAt", transaction.createdAt)
        current = bindNullable(current, "pocketId", transaction.persistedPocketId(), String::class.java)
        current = bindNullable(current, "exchangeRate", transaction.exchangeRate, String::class.java)
        current = bindNullable(current, "destinationAmount", transaction.destinationAmount, Long::class.javaObjectType)
        current = bindNullable(current, "partnerId", transaction.partnerId, String::class.java)
        current = bindNullable(current, "sourcePocketId", transaction.persistedSourcePocketId(), String::class.java)
        current = bindNullable(current, "destinationPocketId", transaction.persistedDestinationPocketId(), String::class.java)
        current = bindNullable(current, "parentTransactionId", transaction.parentTransactionId, String::class.java)
        current = bindNullable(current, "recurringTransactionId", transaction.recurringTransactionId, String::class.java)
        current = bindNullable(current, "modifiedById", transaction.modifiedById, String::class.java)
        return current
    }

    private fun <T : Any> bindNullable(
        spec: DatabaseClient.GenericExecuteSpec,
        name: String,
        value: T?,
        type: Class<T>,
    ): DatabaseClient.GenericExecuteSpec =
        if (value == null) spec.bindNull(name, type) else spec.bind(name, value)

    private fun toTransaction(row: Map<String, Any?>): TransactionRecord = TransactionRecord(
        id = row.getValue("id") as String,
        accountId = row.getValue("account_id") as String,
        type = row.getValue("type") as String,
        status = row.getValue("status") as String,
        transactionDate = row.getValue("transaction_date") as String,
        amount = (row.getValue("amount") as Number).toLong(),
        currencyCode = row.getValue("currency_code") as String,
        exchangeRate = row["exchange_rate"] as String?,
        destinationAmount = (row["destination_amount"] as Number?)?.toLong(),
        description = row.getValue("description") as String,
        partnerId = row["partner_id"] as String?,
        pocketId = row["pocket_id"] as String?,
        sourcePocketId = row["source_pocket_id"] as String?,
        destinationPocketId = row["destination_pocket_id"] as String?,
        parentTransactionId = row["parent_transaction_id"] as String?,
        recurringTransactionId = row["recurring_transaction_id"] as String?,
        modifiedById = row["modified_by_id"] as String?,
        transactionOrigin = row.getValue("transaction_origin") as String,
        isArchived = row.getValue("is_archived") as Boolean,
        createdAt = (row.getValue("created_at") as Number).toLong(),
    )

    private companion object {
        private val visibilityPredicate = TransactionVisibility.SQL_PREDICATE
            .replace("modified_by_id", "t.modified_by_id")
            .replace("is_archived", "t.is_archived")

        private val baseSelect =
            """
            SELECT t.id,
                   COALESCE(p.account_id, sp.account_id, dp.account_id) AS account_id,
                   t.type,
                   t.status,
                   t.transaction_date,
                   t.amount,
                   t.currency_code,
                   t.exchange_rate,
                   t.destination_amount,
                   t.description,
                   t.partner_id,
                   t.pocket_id,
                   CASE WHEN t.type = 'EXPENSE' THEN t.pocket_id ELSE t.source_pocket_id END AS source_pocket_id,
                   CASE WHEN t.type = 'INCOME' THEN t.pocket_id ELSE t.destination_pocket_id END AS destination_pocket_id,
                   t.parent_transaction_id,
                   t.recurring_transaction_id,
                   t.modified_by_id,
                   t.transaction_origin,
                   t.is_archived,
                   t.created_at
            FROM transactions t
            LEFT JOIN pockets p ON p.id = t.pocket_id
            LEFT JOIN pockets sp ON sp.id = t.source_pocket_id
            LEFT JOIN pockets dp ON dp.id = t.destination_pocket_id
            """.trimIndent()
    }
}
