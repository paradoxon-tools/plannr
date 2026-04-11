package de.chennemann.plannr.server.recurringtransactions.persistence

import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransaction
import de.chennemann.plannr.server.recurringtransactions.domain.RecurringTransactionRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class R2dbcRecurringTransactionRepository(
    private val databaseClient: DatabaseClient,
) : RecurringTransactionRepository {
    override suspend fun save(recurringTransaction: RecurringTransaction): RecurringTransaction {
        val spec = databaseClient.sql(
            """
            INSERT INTO recurring_transactions (
                id, source_pocket_id, destination_pocket_id, partner_id, title, description,
                amount, currency_code, transaction_type, first_occurrence_date, final_occurrence_date, recurrence_type,
                skip_count, days_of_week, weeks_of_month, days_of_month, months_of_year, last_materialized_date,
                previous_version_id, is_archived, created_at
            ) VALUES (
                :id, :sourcePocketId, :destinationPocketId, :partnerId, :title, :description,
                :amount, :currencyCode, :transactionType, :firstOccurrenceDate, :finalOccurrenceDate, :recurrenceType,
                :skipCount, :daysOfWeek, :weeksOfMonth, :daysOfMonth, :monthsOfYear, :lastMaterializedDate,
                :previousVersionId, :isArchived, :createdAt
            )
            """.trimIndent(),
        )
        bindAll(spec, recurringTransaction).fetch().rowsUpdated().awaitSingle()
        return recurringTransaction
    }

    override suspend fun update(recurringTransaction: RecurringTransaction): RecurringTransaction {
        val spec = databaseClient.sql(
            """
            UPDATE recurring_transactions SET
                source_pocket_id = :sourcePocketId,
                destination_pocket_id = :destinationPocketId,
                partner_id = :partnerId,
                title = :title,
                description = :description,
                amount = :amount,
                currency_code = :currencyCode,
                transaction_type = :transactionType,
                first_occurrence_date = :firstOccurrenceDate,
                final_occurrence_date = :finalOccurrenceDate,
                recurrence_type = :recurrenceType,
                skip_count = :skipCount,
                days_of_week = :daysOfWeek,
                weeks_of_month = :weeksOfMonth,
                days_of_month = :daysOfMonth,
                months_of_year = :monthsOfYear,
                last_materialized_date = :lastMaterializedDate,
                previous_version_id = :previousVersionId,
                is_archived = :isArchived,
                created_at = :createdAt
            WHERE id = :id
            """.trimIndent(),
        )
        bindAll(spec, recurringTransaction).fetch().rowsUpdated().awaitSingle()
        return recurringTransaction
    }

    override suspend fun findById(id: String): RecurringTransaction? = databaseClient.sql(selectSql("WHERE id = :id"))
        .bind("id", id)
        .fetch().one().map(::toRecurringTransaction).awaitSingleOrNull()

    override suspend fun findAll(accountId: String?, contractId: String?, archived: Boolean): List<RecurringTransaction> {
        val conditions = mutableListOf("rt.is_archived = :archived")
        if (accountId != null) conditions += "COALESCE(sp.account_id, dp.account_id) = :accountId"
        if (contractId != null) conditions += "c.id = :contractId"
        var spec = databaseClient.sql(selectSql("WHERE ${conditions.joinToString(" AND ")}"))
            .bind("archived", archived)
        if (accountId != null) spec = spec.bind("accountId", accountId)
        if (contractId != null) spec = spec.bind("contractId", contractId)
        return spec.fetch().all().map(::toRecurringTransaction).collectList().awaitSingle()
    }

    override suspend fun findByContractId(contractId: String): List<RecurringTransaction> =
        databaseClient.sql(selectSql("WHERE c.id = :contractId"))
            .bind("contractId", contractId)
            .fetch().all().map(::toRecurringTransaction).collectList().awaitSingle()

    override suspend fun findByPreviousVersionId(previousVersionId: String): List<RecurringTransaction> =
        databaseClient.sql(selectSql("WHERE previous_version_id = :previousVersionId"))
            .bind("previousVersionId", previousVersionId)
            .fetch().all().map(::toRecurringTransaction).collectList().awaitSingle()

    private fun selectSql(whereClause: String) =
        """
        SELECT rt.id,
               c.id AS contract_id,
               COALESCE(sp.account_id, dp.account_id) AS account_id,
               rt.source_pocket_id,
               rt.destination_pocket_id,
               rt.partner_id,
               rt.title,
               rt.description,
               rt.amount,
               rt.currency_code,
               rt.transaction_type,
               rt.first_occurrence_date,
               rt.final_occurrence_date,
               rt.recurrence_type,
               rt.skip_count,
               rt.days_of_week,
               rt.weeks_of_month,
               rt.days_of_month,
               rt.months_of_year,
               rt.last_materialized_date,
               rt.previous_version_id,
               rt.is_archived,
               rt.created_at
        FROM recurring_transactions rt
        LEFT JOIN pockets sp ON sp.id = rt.source_pocket_id
        LEFT JOIN pockets dp ON dp.id = rt.destination_pocket_id
        LEFT JOIN contracts c ON c.pocket_id = rt.source_pocket_id OR c.pocket_id = rt.destination_pocket_id
        $whereClause
        ORDER BY rt.created_at ASC, rt.id ASC
        """.trimIndent()

    private fun bindAll(spec: DatabaseClient.GenericExecuteSpec, recurringTransaction: RecurringTransaction): DatabaseClient.GenericExecuteSpec {
        var current = spec
            .bind("id", recurringTransaction.id)
            .bind("title", recurringTransaction.title)
            .bind("amount", recurringTransaction.amount)
            .bind("currencyCode", recurringTransaction.currencyCode)
            .bind("transactionType", recurringTransaction.transactionType)
            .bind("firstOccurrenceDate", recurringTransaction.firstOccurrenceDate)
            .bind("recurrenceType", recurringTransaction.recurrenceType)
            .bind("skipCount", recurringTransaction.skipCount)
            .bind("isArchived", recurringTransaction.isArchived)
            .bind("createdAt", recurringTransaction.createdAt)
        current = bindNullable(current, "sourcePocketId", recurringTransaction.sourcePocketId)
        current = bindNullable(current, "destinationPocketId", recurringTransaction.destinationPocketId)
        current = bindNullable(current, "partnerId", recurringTransaction.partnerId)
        current = bindNullable(current, "description", recurringTransaction.description)
        current = bindNullable(current, "finalOccurrenceDate", recurringTransaction.finalOccurrenceDate)
        current = bindNullable(current, "daysOfWeek", recurringTransaction.daysOfWeek?.joinToString(","))
        current = bindNullable(current, "weeksOfMonth", recurringTransaction.weeksOfMonth?.joinToString(","))
        current = bindNullable(current, "daysOfMonth", recurringTransaction.daysOfMonth?.joinToString(","))
        current = bindNullable(current, "monthsOfYear", recurringTransaction.monthsOfYear?.joinToString(","))
        current = bindNullable(current, "lastMaterializedDate", recurringTransaction.lastMaterializedDate)
        current = bindNullable(current, "previousVersionId", recurringTransaction.previousVersionId)
        return current
    }

    private fun bindNullable(spec: DatabaseClient.GenericExecuteSpec, name: String, value: String?): DatabaseClient.GenericExecuteSpec =
        if (value != null) spec.bind(name, value) else spec.bindNull(name, String::class.java)

    private fun toRecurringTransaction(row: Map<String, Any?>): RecurringTransaction = RecurringTransaction(
        id = row.getValue("id") as String,
        contractId = row["contract_id"] as String?,
        accountId = row.getValue("account_id") as String,
        sourcePocketId = row["source_pocket_id"] as String?,
        destinationPocketId = row["destination_pocket_id"] as String?,
        partnerId = row["partner_id"] as String?,
        title = row.getValue("title") as String,
        description = row["description"] as String?,
        amount = (row.getValue("amount") as Number).toLong(),
        currencyCode = row.getValue("currency_code") as String,
        transactionType = row.getValue("transaction_type") as String,
        firstOccurrenceDate = row.getValue("first_occurrence_date") as String,
        finalOccurrenceDate = row["final_occurrence_date"] as String?,
        recurrenceType = row.getValue("recurrence_type") as String,
        skipCount = (row.getValue("skip_count") as Number).toInt(),
        daysOfWeek = (row["days_of_week"] as String?)?.split(',')?.filter { it.isNotBlank() },
        weeksOfMonth = (row["weeks_of_month"] as String?)?.split(',')?.filter { it.isNotBlank() }?.map(String::toInt),
        daysOfMonth = (row["days_of_month"] as String?)?.split(',')?.filter { it.isNotBlank() }?.map(String::toInt),
        monthsOfYear = (row["months_of_year"] as String?)?.split(',')?.filter { it.isNotBlank() }?.map(String::toInt),
        lastMaterializedDate = row["last_materialized_date"] as String?,
        previousVersionId = row["previous_version_id"] as String?,
        isArchived = row.getValue("is_archived") as Boolean,
        createdAt = (row.getValue("created_at") as Number).toLong(),
    )
}
