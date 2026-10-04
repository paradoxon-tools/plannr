package de.chennemann.plannr.server.banking

import de.chennemann.plannr.server.common.error.ConflictException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@Service
class ReconciliationService(private val db: BankingDatabase) {
    suspend fun transactions(bankAccountId: Long, status: String?, from: LocalDate?, to: LocalDate?, limit: Int, offset: Int): List<BankTransaction> {
        page(limit, offset)
        checkInput(status == null || status in setOf("UNMATCHED", "IGNORED", "RECONCILED"), "Invalid reconciliation status")
        checkInput(from == null || to == null || !from.isAfter(to), "Invalid date range")
        db.account(bankAccountId)
        val filters = mutableListOf("t.bank_account_id=:account")
        val values = mutableListOf<Pair<String, Any?>>("account" to bankAccountId, "limit" to limit, "offset" to offset)
        if (status != null) filters += when (status) {
            "RECONCILED" -> "r.bank_transaction_id IS NOT NULL"
            "IGNORED" -> "r.bank_transaction_id IS NULL AND t.ignored"
            else -> "r.bank_transaction_id IS NULL AND NOT t.ignored"
        }
        from?.let { filters += "COALESCE(t.booking_date,t.transaction_date,t.value_date)>=CAST(:from AS date)"; values += "from" to it.toString() }
        to?.let { filters += "COALESCE(t.booking_date,t.transaction_date,t.value_date)<=CAST(:to AS date)"; values += "to" to it.toString() }
        return db.rows("""SELECT t.*, r.bank_transaction_id IS NOT NULL AS reconciled FROM bank_transactions t
            LEFT JOIN bank_reconciliations r ON r.bank_transaction_id=t.id WHERE ${filters.joinToString(" AND ")}
            ORDER BY COALESCE(t.booking_date,t.transaction_date,t.value_date) DESC NULLS LAST,t.id DESC LIMIT :limit OFFSET :offset""",
            *values.toTypedArray()).map { it.actual() }
    }

    suspend fun get(id: Long) = db.actual(id).actual()

    suspend fun planned(accountId: Long, from: LocalDate, to: LocalDate, limit: Int, offset: Int): List<PlannedTransaction> {
        page(limit, offset)
        checkInput(!from.isAfter(to), "Invalid date range")
        db.one("SELECT id FROM accounts WHERE id=:id", "id" to accountId)
        return db.planned(accountId, from.toString(), to.toString(), limit, offset).map { it.planned() }
    }

    suspend fun suggestions(id: Long, days: Int): List<MatchSuggestion> {
        checkInput(days in 0..31, "days must be between 0 and 31")
        val actual = get(id)
        if (actual.bookingStatus != "BOOK" || actual.reconciliationStatus != "UNMATCHED" || !actual.isCurrent) return emptyList()
        val accountId = db.account(actual.bankAccountId).optionalLong("account_id") ?: return emptyList()
        val date = actual.bookingDate ?: actual.transactionDate ?: actual.valueDate ?: return emptyList()
        return db.planned(accountId, date.minusDays(days.toLong()).toString(), date.plusDays(days.toLong()).toString(), 500, 0)
            .map { it.planned() }.filter { it.reconciliationStatus == "PLANNED" && it.currencyCode == actual.currencyCode && it.amount.signum() == actual.amount.signum() }
            .map { MatchSuggestion(it, actual.amount.subtract(it.amount), abs(ChronoUnit.DAYS.between(LocalDate.parse(it.occurrenceDate), date))) }
            .sortedWith(compareBy<MatchSuggestion> { it.amountDifference.abs() }.thenBy { it.daysDifference }.thenBy { it.planned.materializationId }).take(20)
    }

    suspend fun reconcile(id: Long, command: ReconcileRequest): Reconciliation = db.transaction {
        val actualRow = db.actual(id, true)
        val actual = actualRow.actual()
        val accountId = db.account(actual.bankAccountId, true).optionalLong("account_id")
            ?: throw ConflictException("bank_account_unlinked", "Link the bank account to a planning account first")
        checkInput(actual.bookingStatus == "BOOK" && actual.isCurrent, "Only current booked transactions can be reconciled")
        checkInput(!actualRow.bool("ignored"), "Restore the ignored transaction before reconciling it")
        checkInput(command.note == null || command.note.length <= 2000, "note must not exceed 2000 characters")
        val plannedRow = db.plannedById(accountId, command.materializationId)
        val planned = plannedRow.planned()
        checkInput(actual.currencyCode == planned.currencyCode, "Transaction currencies must match")
        checkInput(actual.amount.signum() == planned.amount.signum(), "Transaction directions must match")
        checkInput(command.acceptAmountDifference || actual.amount.compareTo(planned.amount) == 0, "Confirm acceptAmountDifference to reconcile different amounts")
        if (actual.reconciliationStatus == "RECONCILED") {
            val existing = reconciliation(id)
            if (existing.templateVersionId == planned.templateVersionId && existing.occurrenceDate == planned.occurrenceDate) return@transaction existing
            throw ConflictException("transaction_already_reconciled", "Undo the existing reconciliation first")
        }
        db.execute("""INSERT INTO bank_reconciliations(bank_transaction_id,account_id,template_version_id,occurrence_date,
            planned_amount,planned_snapshot,note,created_at) VALUES(:id,:account,:version,:date,:amount,CAST(:snapshot AS jsonb),:note,:now)""",
            "id" to id, "account" to accountId, "version" to planned.templateVersionId, "date" to planned.occurrenceDate,
            "amount" to planned.amount, "snapshot" to plannedRow.string("snapshot"), "note" to command.note, "now" to System.currentTimeMillis())
        reconciliation(id)
    }

    suspend fun reconciliation(id: Long): Reconciliation = db.one("""SELECT r.*,t.amount AS actual_amount
        FROM bank_reconciliations r JOIN bank_transactions t ON t.id=r.bank_transaction_id WHERE r.bank_transaction_id=:id""", "id" to id).let(::toReconciliation)

    suspend fun reconciliations(accountId: Long, limit: Int, offset: Int): List<Reconciliation> {
        page(limit, offset)
        return db.rows("""SELECT r.*,t.amount AS actual_amount FROM bank_reconciliations r
            JOIN bank_transactions t ON t.id=r.bank_transaction_id WHERE r.account_id=:id
            ORDER BY r.created_at DESC,r.bank_transaction_id DESC LIMIT :limit OFFSET :offset""",
            "id" to accountId, "limit" to limit, "offset" to offset).map(::toReconciliation)
    }

    private fun toReconciliation(row: BankRow) = Reconciliation(row.long("bank_transaction_id"), row.long("account_id"),
        row.long("template_version_id"), row.string("occurrence_date"), row.decimal("planned_amount"), row.decimal("actual_amount"),
        row.decimal("actual_amount").subtract(row.decimal("planned_amount")), row.json("planned_snapshot"), row.text("note"), row.long("created_at"))

    suspend fun undo(id: Long): BankTransaction = db.transaction {
        db.actual(id, true)
        db.execute("DELETE FROM bank_reconciliations WHERE bank_transaction_id=:id", "id" to id)
        get(id)
    }

    suspend fun ignore(id: Long, command: IgnoreRequest): BankTransaction = db.transaction {
        if (db.actual(id, true).bool("reconciled")) throw ConflictException("transaction_already_reconciled", "Undo reconciliation before changing review state")
        db.execute("UPDATE bank_transactions SET ignored=:ignored WHERE id=:id", "id" to id, "ignored" to command.ignored)
        get(id)
    }

    private fun page(limit: Int, offset: Int) = checkInput(limit in 1..500 && offset >= 0, "limit must be 1..500 and offset must be nonnegative")
}
