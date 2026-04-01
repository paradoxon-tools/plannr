package de.chennemann.plannr.server.accounts.persistence

import de.chennemann.plannr.server.accounts.domain.Account
import de.chennemann.plannr.server.accounts.domain.AccountRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class R2dbcAccountRepository(
    private val databaseClient: DatabaseClient,
) : AccountRepository {
    override suspend fun save(account: Account): Account {
        databaseClient.sql(
            """
            INSERT INTO accounts (id, name, institution, currency_code, weekend_handling, is_archived, created_at)
            VALUES (:id, :name, :institution, :currencyCode, :weekendHandling, :isArchived, :createdAt)
            """.trimIndent(),
        )
            .bind("id", account.id)
            .bind("name", account.name)
            .bind("institution", account.institution)
            .bind("currencyCode", account.currencyCode)
            .bind("weekendHandling", account.weekendHandling)
            .bind("isArchived", account.isArchived)
            .bind("createdAt", account.createdAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        return account
    }

    override suspend fun update(account: Account): Account {
        databaseClient.sql(
            """
            UPDATE accounts
            SET name = :name,
                institution = :institution,
                currency_code = :currencyCode,
                weekend_handling = :weekendHandling,
                is_archived = :isArchived
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", account.id)
            .bind("name", account.name)
            .bind("institution", account.institution)
            .bind("currencyCode", account.currencyCode)
            .bind("weekendHandling", account.weekendHandling)
            .bind("isArchived", account.isArchived)
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        return account
    }

    override suspend fun findById(id: String): Account? =
        databaseClient.sql(
            """
            SELECT id, name, institution, currency_code, weekend_handling, is_archived, created_at
            FROM accounts
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", id)
            .fetch()
            .one()
            .map(::toAccount)
            .awaitSingleOrNull()

    override suspend fun findAll(): List<Account> =
        databaseClient.sql(
            """
            SELECT id, name, institution, currency_code, weekend_handling, is_archived, created_at
            FROM accounts
            ORDER BY created_at ASC, id ASC
            """.trimIndent(),
        )
            .fetch()
            .all()
            .map(::toAccount)
            .collectList()
            .awaitSingle()

    private fun toAccount(row: Map<String, Any>): Account =
        Account(
            id = row.getValue("id") as String,
            name = row.getValue("name") as String,
            institution = row.getValue("institution") as String,
            currencyCode = row.getValue("currency_code") as String,
            weekendHandling = row.getValue("weekend_handling") as String,
            isArchived = row.getValue("is_archived") as Boolean,
            createdAt = (row.getValue("created_at") as Number).toLong(),
        )
}
