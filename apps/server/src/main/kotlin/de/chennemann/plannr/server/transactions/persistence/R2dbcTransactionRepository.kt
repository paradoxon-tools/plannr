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
                    id, account_id, type, status, transaction_date, amount, currency_code, exchange_rate,
                    destination_amount, description, partner_id, source_pocket_id, destination_pocket_id,
                    parent_transaction_id, recurring_transaction_id, modified_by_id, transaction_origin, is_archived, created_at
                ) VALUES (
                    :id, :accountId, :type, :status, :transactionDate, :amount, :currencyCode, :exchangeRate,
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
                SET account_id = :accountId,
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
        databaseClient.sql("SELECT * FROM transactions WHERE id = :id")
            .bind("id", id)
            .fetch()
            .one()
            .map(::toTransaction)
            .awaitSingleOrNull()

    override suspend fun findVisibleByAccountId(accountId: String): List<TransactionRecord> =
        findAll(
            """
            WHERE account_id = :scopeId
              AND ${TransactionVisibility.SQL_PREDICATE}
            ORDER BY transaction_date ASC, created_at ASC, id ASC
            """.trimIndent(),
            accountId,
        )

    override suspend fun findVisibleByPocketId(pocketId: String): List<TransactionRecord> =
        findAll(
            """
            WHERE (source_pocket_id = :scopeId OR destination_pocket_id = :scopeId)
              AND ${TransactionVisibility.SQL_PREDICATE}
            ORDER BY transaction_date ASC, created_at ASC, id ASC
            """.trimIndent(),
            pocketId,
        )

    override suspend fun findVisibleByRecurringTransactionId(recurringTransactionId: String): List<TransactionRecord> =
        findAll(
            """
            WHERE recurring_transaction_id = :scopeId
              AND ${TransactionVisibility.SQL_PREDICATE}
            ORDER BY transaction_date ASC, created_at ASC, id ASC
            """.trimIndent(),
            recurringTransactionId,
        )

    override suspend fun findVisiblePending(): List<TransactionRecord> =
        databaseClient.sql(
            """
            SELECT * FROM transactions
            WHERE ${TransactionVisibility.SQL_PREDICATE}
              AND status = 'PENDING'
            ORDER BY transaction_date ASC, created_at ASC, id ASC
            """.trimIndent(),
        )
            .fetch()
            .all()
            .let { rows -> Flux.from(rows).map(::toTransaction).collectList().awaitSingle() }

    override suspend fun findVisibleFutureByAccountId(accountId: String, startDateInclusive: String, endDateInclusive: String): List<TransactionRecord> =
        findAllByScopeAndDateRange(
            "account_id = :scopeId",
            accountId,
            startDateInclusive,
            endDateInclusive,
        )

    override suspend fun findVisibleFutureByPocketId(pocketId: String, startDateInclusive: String, endDateInclusive: String): List<TransactionRecord> =
        findAllByScopeAndDateRange(
            "(source_pocket_id = :scopeId OR destination_pocket_id = :scopeId)",
            pocketId,
            startDateInclusive,
            endDateInclusive,
        )

    private suspend fun findAll(whereClause: String, scopeId: String): List<TransactionRecord> =
        databaseClient.sql("SELECT * FROM transactions $whereClause")
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
            SELECT * FROM transactions
            WHERE $scopePredicate
              AND ${TransactionVisibility.SQL_PREDICATE}
              AND transaction_date BETWEEN :startDateInclusive AND :endDateInclusive
            ORDER BY transaction_date ASC, created_at ASC, id ASC
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
            .bind("accountId", transaction.accountId)
            .bind("type", transaction.type)
            .bind("status", transaction.status)
            .bind("transactionDate", transaction.transactionDate)
            .bind("amount", transaction.amount)
            .bind("currencyCode", transaction.currencyCode)
            .bind("description", transaction.description)
            .bind("transactionOrigin", transaction.transactionOrigin)
            .bind("isArchived", transaction.isArchived)
            .bind("createdAt", transaction.createdAt)
        current = bindNullable(current, "exchangeRate", transaction.exchangeRate, String::class.java)
        current = bindNullable(current, "destinationAmount", transaction.destinationAmount, Long::class.javaObjectType)
        current = bindNullable(current, "partnerId", transaction.partnerId, String::class.java)
        current = bindNullable(current, "sourcePocketId", transaction.sourcePocketId, String::class.java)
        current = bindNullable(current, "destinationPocketId", transaction.destinationPocketId, String::class.java)
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
        sourcePocketId = row["source_pocket_id"] as String?,
        destinationPocketId = row["destination_pocket_id"] as String?,
        parentTransactionId = row["parent_transaction_id"] as String?,
        recurringTransactionId = row["recurring_transaction_id"] as String?,
        modifiedById = row["modified_by_id"] as String?,
        transactionOrigin = row.getValue("transaction_origin") as String,
        isArchived = row.getValue("is_archived") as Boolean,
        createdAt = (row.getValue("created_at") as Number).toLong(),
    )
}
