package de.chennemann.plannr.server.banking

import de.chennemann.plannr.server.common.error.ApiException
import de.chennemann.plannr.server.common.error.ConflictException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class BankSyncService(private val db: BankingDatabase, private val provider: BankingProvider) {
    suspend fun sync(id: Long, command: SyncRequest): SyncResult {
        checkInput(!command.dateFrom.isAfter(command.dateTo), "dateFrom must be on or before dateTo")
        checkInput(!command.dateTo.isAfter(LocalDate.now(java.time.ZoneOffset.UTC)), "dateTo must not be in the future")
        checkInput(java.time.temporal.ChronoUnit.DAYS.between(command.dateFrom, command.dateTo) <= 366, "Sync at most 366 days at a time")
        try {
            return db.transaction {
                val account = db.account(id, true)
                val connection = db.connection(account.uuid("connection_id"), true)
                if (connection.connection(System.currentTimeMillis()).status != "ACTIVE") {
                    throw ConflictException("bank_connection_inactive", "Renew bank consent before syncing")
                }
                val uid = account.string("provider_uid")
                val importer = BankTransactionImport()
                val transactions = mutableListOf<ImportedTransaction>()
                val seenKeys = mutableSetOf<String>()
                var continuation: String? = null
                var pages = 0
                do {
                    val query = mutableMapOf("date_from" to command.dateFrom.toString(), "date_to" to command.dateTo.toString())
                    continuation?.let { query["continuation_key"] = it }
                    val response = provider.request("GET", "/accounts/$uid/transactions", query = query)
                    val items = response.get("transactions")?.takeIf { it.isArray } ?: providerFailure()
                    items.forEach { transactions.add(importer.parse(it)) }
                    continuation = response.textOrNull("continuation_key")
                    pages++
                    if (continuation != null && (!seenKeys.add(continuation) || pages >= 100) || transactions.size > 100_000) providerFailure()
                } while (continuation != null)
                val balances = provider.request("GET", "/accounts/$uid/balances").get("balances")?.takeIf { it.isArray } ?: providerFailure()
                val now = System.currentTimeMillis()
                // Complete fetch before any mutations. A failed later page cannot publish a partial import.
                // Unreferenced entries are snapshots, not trustworthy permanent identities; retain old observations for review.
                db.execute("""UPDATE bank_transactions SET is_current=false WHERE bank_account_id=:id
                    AND (booking_status IN ('PDNG','HOLD','SCHD') OR identity_quality='FINGERPRINT')
                    AND COALESCE(booking_date,transaction_date,value_date) BETWEEN CAST(:from AS date) AND CAST(:to AS date)""",
                    "id" to id, "from" to command.dateFrom.toString(), "to" to command.dateTo.toString())
                transactions.forEach { save(id, it, now) }
                db.execute("UPDATE bank_accounts SET balances=CAST(:balances AS jsonb),last_synced_at=:now,last_error=NULL WHERE id=:id",
                    "id" to id, "balances" to balances.toString(), "now" to now)
                SyncResult(transactions.size, now)
            }
        } catch (e: ApiException) {
            // A busy response must not overwrite the result of the operation currently holding the lock.
            if (e.code != "banking_busy") db.execute("UPDATE bank_accounts SET last_error=:error WHERE id=:id", "id" to id, "error" to e.code)
            throw e
        }
    }

    private suspend fun save(id: Long, tx: ImportedTransaction, now: Long) {
        val existing = db.rows("""SELECT t.*, EXISTS(SELECT 1 FROM bank_reconciliations r WHERE r.bank_transaction_id=t.id) AS reconciled
            FROM bank_transactions t WHERE bank_account_id=:account AND import_key=:key""", "account" to id, "key" to tx.key).singleOrNull()
        if (existing != null && existing.bool("reconciled") &&
            (existing.decimal("amount").compareTo(tx.amount) != 0 || existing.string("currency_code") != tx.currency || tx.status != "BOOK")) {
            throw ConflictException("reconciled_bank_transaction_changed", "A reconciled bank transaction changed; undo its reconciliation before syncing again")
        }
        // An overlapping pending response must never downgrade an already booked stable entry.
        if (existing?.string("booking_status") == "BOOK" && tx.status in setOf("PDNG", "HOLD", "SCHD")) return
        db.execute("""INSERT INTO bank_transactions(bank_account_id,import_key,entry_reference,identity_quality,
                booking_status,amount,currency_code,booking_date,value_date,transaction_date,counterparty,description,
                raw_data,first_seen_at,last_seen_at)
            VALUES(:account,:key,:reference,:quality,:status,:amount,:currency,CAST(:booking AS date),CAST(:value AS date),
                CAST(:date AS date),:party,:description,CAST(:raw AS jsonb),:now,:now)
            ON CONFLICT(bank_account_id,import_key) DO UPDATE SET booking_status=EXCLUDED.booking_status,
                amount=EXCLUDED.amount,currency_code=EXCLUDED.currency_code,booking_date=EXCLUDED.booking_date,
                value_date=EXCLUDED.value_date,transaction_date=EXCLUDED.transaction_date,counterparty=EXCLUDED.counterparty,
                description=EXCLUDED.description,raw_data=EXCLUDED.raw_data,last_seen_at=EXCLUDED.last_seen_at,is_current=true""",
            "account" to id, "key" to tx.key, "reference" to tx.reference, "quality" to tx.identityQuality,
            "status" to tx.status, "amount" to tx.amount, "currency" to tx.currency, "booking" to tx.bookingDate,
            "value" to tx.valueDate, "date" to tx.transactionDate, "party" to tx.counterparty,
            "description" to tx.description, "raw" to tx.raw.toString(), "now" to now)
    }
}
