package de.chennemann.plannr.server.banking

import de.chennemann.plannr.server.common.error.ConflictException
import de.chennemann.plannr.server.common.error.NotFoundException
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/** All values use bind parameters. Transactions and database locks coordinate multiple server instances. */
@Component
class BankingDatabase(client: DatabaseClient, manager: ReactiveTransactionManager, val mapper: ObjectMapper) {
    private val client = client
    private val transactions = TransactionalOperator.create(manager)

    suspend fun <T> transaction(block: suspend () -> T): T = try {
        transactions.executeAndAwait {
            if (!one("SELECT pg_try_advisory_xact_lock(724891230) AS acquired").bool("acquired")) {
                throw ConflictException("banking_busy", "Another banking operation is running; retry shortly")
            }
            block()
        }
    } catch (e: DataIntegrityViolationException) {
        throw ConflictException("banking_conflict", "This account or planned occurrence is already linked, or the referenced record changed")
    }

    suspend fun rows(sql: String, vararg values: Pair<String, Any?>): List<BankRow> {
        var statement = client.sql(sql)
        values.forEach { (key, value) -> statement = if (value == null) statement.bindNull(key, String::class.java) else statement.bind(key, value) }
        return statement.map { row: Row, metadata: RowMetadata ->
            BankRow(metadata.columnMetadatas.associate { it.name to row.get(it.name) }, mapper)
        }.all().collectList().awaitSingle()
    }

    suspend fun one(sql: String, vararg values: Pair<String, Any?>): BankRow = rows(sql, *values).singleOrNull()
        ?: throw NotFoundException("banking_not_found", "Banking record not found")

    suspend fun execute(sql: String, vararg values: Pair<String, Any?>): Long {
        var statement = client.sql(sql)
        values.forEach { (key, value) -> statement = if (value == null) statement.bindNull(key, String::class.java) else statement.bind(key, value) }
        return statement.fetch().rowsUpdated().awaitSingle()
    }

    suspend fun connection(id: UUID, lock: Boolean = false) = one("SELECT * FROM bank_connections WHERE id = :id" + if (lock) " FOR UPDATE" else "", "id" to id)
    suspend fun account(id: Long, lock: Boolean = false) = one("SELECT * FROM bank_accounts WHERE id = :id" + if (lock) " FOR UPDATE" else "", "id" to id)
    suspend fun actual(id: Long, lock: Boolean = false) = one("""
        SELECT t.*, EXISTS(SELECT 1 FROM bank_reconciliations r WHERE r.bank_transaction_id=t.id) AS reconciled
        FROM bank_transactions t WHERE t.id=:id ${if (lock) "FOR UPDATE OF t" else ""}
    """, "id" to id)

    suspend fun planned(accountId: Long, from: String, to: String, limit: Int, offset: Int) = rows("""
        SELECT m.*, CASE WHEN sp.account_id=:account THEN -m.amount ELSE m.amount END AS signed_amount,
          EXISTS(SELECT 1 FROM bank_reconciliations r WHERE r.account_id=:account
            AND r.template_version_id=m.transaction_template_version_id AND r.occurrence_date=m.transaction_date) AS reconciled
        FROM transaction_materializations m
        LEFT JOIN pockets sp ON sp.id=m.source_pocket_id
        LEFT JOIN pockets dp ON dp.id=m.destination_pocket_id
        WHERE (sp.account_id=:account OR dp.account_id=:account)
          AND NOT (COALESCE(sp.account_id=:account,false) AND COALESCE(dp.account_id=:account,false))
          AND m.transaction_date BETWEEN :from AND :to
        ORDER BY m.transaction_date, m.id LIMIT :limit OFFSET :offset
    """, "account" to accountId, "from" to from, "to" to to, "limit" to limit, "offset" to offset)

    suspend fun plannedById(accountId: Long, id: Long) = one("""
        SELECT m.*, CASE WHEN sp.account_id=:account THEN -m.amount ELSE m.amount END AS signed_amount,
          false AS reconciled, row_to_json(m)::text AS snapshot
        FROM transaction_materializations m
        LEFT JOIN pockets sp ON sp.id=m.source_pocket_id
        LEFT JOIN pockets dp ON dp.id=m.destination_pocket_id
        WHERE m.id=:id AND (sp.account_id=:account OR dp.account_id=:account)
          AND NOT (COALESCE(sp.account_id=:account,false) AND COALESCE(dp.account_id=:account,false))
        FOR SHARE OF m
    """, "account" to accountId, "id" to id)
}

class BankRow(private val values: Map<String, Any?>, private val mapper: ObjectMapper) {
    fun text(key: String): String? = values[key]?.toString()
    fun string(key: String): String = requireNotNull(text(key)) { "Missing column $key" }
    fun long(key: String): Long = (values[key] as Number).toLong()
    fun optionalLong(key: String): Long? = (values[key] as? Number)?.toLong()
    fun bool(key: String): Boolean = values[key] as Boolean
    fun uuid(key: String): UUID = values[key] as UUID
    fun decimal(key: String): BigDecimal = values[key].let { if (it is BigDecimal) it else BigDecimal(it.toString()) }
    fun date(key: String): LocalDate? = text(key)?.let(LocalDate::parse)
    fun json(key: String) = mapper.readTree(when (val value = values[key]) {
        is io.r2dbc.postgresql.codec.Json -> value.asString()
        else -> value.toString()
    })
    fun connection(now: Long) = BankConnection(uuid("id"), string("bank_name"), string("country"),
        if ((string("status") == "ACTIVE" && (optionalLong("valid_until") ?: 0) <= now) ||
            (string("status") == "AUTHORIZING" && (optionalLong("state_expires_at") ?: 0) <= now)) "EXPIRED" else string("status"),
        optionalLong("valid_until"), long("created_at"), text("last_error"))
    fun account() = BankAccount(long("id"), uuid("connection_id"), string("name"), text("iban"), string("currency_code"),
        optionalLong("account_id"), json("balances"), optionalLong("last_synced_at"), text("last_error"))
    fun actual() = BankTransaction(long("id"), long("bank_account_id"), text("entry_reference"), string("identity_quality"),
        string("booking_status"), decimal("amount"), string("currency_code"), date("booking_date"), date("value_date"), date("transaction_date"),
        text("counterparty"), string("description"), if (bool("reconciled")) "RECONCILED" else if (bool("ignored")) "IGNORED" else "UNMATCHED",
        bool("is_current"), long("first_seen_at"), long("last_seen_at"))
    fun planned() = PlannedTransaction(long("id"), long("transaction_template_version_id"), string("transaction_date"), string("title"),
        decimal("signed_amount").movePointLeft(de.chennemann.plannr.server.common.domain.Currency.from(string("currency_code")).decimalPlaces),
        string("currency_code"), if (bool("reconciled")) "RECONCILED" else "PLANNED")
}
