package de.chennemann.plannr.server.query.accounts.persistence

import de.chennemann.plannr.server.query.accounts.domain.AccountQuery
import de.chennemann.plannr.server.query.accounts.domain.AccountQueryRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class R2dbcAccountQueryRepository(
    private val databaseClient: DatabaseClient,
) : AccountQueryRepository {
    override suspend fun saveOrUpdate(accountQuery: AccountQuery): AccountQuery {
        val updatedRows = databaseClient.sql(
            """
            UPDATE account_query
            SET name = :name,
                institution = :institution,
                currency_code = :currencyCode,
                weekend_handling = :weekendHandling,
                is_archived = :isArchived,
                created_at = :createdAt,
                current_balance = :currentBalance
            WHERE account_id = :accountId
            """.trimIndent(),
        )
            .bind("accountId", accountQuery.accountId)
            .bind("name", accountQuery.name)
            .bind("institution", accountQuery.institution)
            .bind("currencyCode", accountQuery.currencyCode)
            .bind("weekendHandling", accountQuery.weekendHandling)
            .bind("isArchived", accountQuery.isArchived)
            .bind("createdAt", accountQuery.createdAt)
            .bind("currentBalance", accountQuery.currentBalance)
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        if (updatedRows == 0L) {
            databaseClient.sql(
                """
                INSERT INTO account_query (
                    account_id,
                    name,
                    institution,
                    currency_code,
                    weekend_handling,
                    is_archived,
                    created_at,
                    current_balance
                )
                VALUES (
                    :accountId,
                    :name,
                    :institution,
                    :currencyCode,
                    :weekendHandling,
                    :isArchived,
                    :createdAt,
                    :currentBalance
                )
                """.trimIndent(),
            )
                .bind("accountId", accountQuery.accountId)
                .bind("name", accountQuery.name)
                .bind("institution", accountQuery.institution)
                .bind("currencyCode", accountQuery.currencyCode)
                .bind("weekendHandling", accountQuery.weekendHandling)
                .bind("isArchived", accountQuery.isArchived)
                .bind("createdAt", accountQuery.createdAt)
                .bind("currentBalance", accountQuery.currentBalance)
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        }

        return findById(accountQuery.accountId) ?: accountQuery
    }

    override suspend fun findById(accountId: String): AccountQuery? =
        databaseClient.sql(
            """
            SELECT account_id, name, institution, currency_code, weekend_handling, is_archived, created_at, current_balance
            FROM account_query
            WHERE account_id = :accountId
            """.trimIndent(),
        )
            .bind("accountId", accountId)
            .fetch()
            .one()
            .map(::toAccountQuery)
            .awaitSingleOrNull()

    private fun toAccountQuery(row: Map<String, Any?>): AccountQuery = AccountQuery(
        accountId = row.getValue("account_id") as String,
        name = row.getValue("name") as String,
        institution = row.getValue("institution") as String,
        currencyCode = row.getValue("currency_code") as String,
        weekendHandling = row.getValue("weekend_handling") as String,
        isArchived = row.getValue("is_archived") as Boolean,
        createdAt = (row.getValue("created_at") as Number).toLong(),
        currentBalance = (row.getValue("current_balance") as Number).toLong(),
    )
}
