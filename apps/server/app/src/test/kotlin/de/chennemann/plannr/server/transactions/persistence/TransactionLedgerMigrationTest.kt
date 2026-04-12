package de.chennemann.plannr.server.transactions.persistence

import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import kotlin.test.assertEquals

class TransactionLedgerMigrationTest : ApiIntegrationTest() {
    @Autowired lateinit var databaseClient: DatabaseClient

    @Test
    fun `latest migrations apply on empty schema`() = runBlocking {
        flyway().clean()
        flyway().migrate()

        assertEquals(true, hasTransactionsColumn("pocket_id"))
        assertEquals(false, hasTransactionsColumn("account_id"))
        assertEquals(false, hasRecurringTransactionsColumn("account_id"))
        assertEquals(false, hasRecurringTransactionsColumn("contract_id"))
    }

    @Test
    fun `latest migrations apply over current server schema state`() = runBlocking {
        flyway().clean()
        flyway(target = "8").migrate()
        flyway().migrate()

        assertEquals(true, hasTransactionsColumn("pocket_id"))
        assertEquals(false, hasTransactionsColumn("account_id"))
        assertEquals(false, hasRecurringTransactionsColumn("account_id"))
        assertEquals(false, hasRecurringTransactionsColumn("contract_id"))
    }

    private fun flyway(target: String? = null): Flyway {
        val configuration = Flyway.configure()
            .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
            .locations("classpath:db/migration")
            .cleanDisabled(false)
        if (target != null) {
            configuration.target(target)
        }
        return configuration.load()
    }

    private suspend fun hasTransactionsColumn(columnName: String): Boolean = hasColumn("transactions", columnName)

    private suspend fun hasRecurringTransactionsColumn(columnName: String): Boolean = hasColumn("recurring_transactions", columnName)

    private suspend fun hasColumn(tableName: String, columnName: String): Boolean =
        ((databaseClient.sql(
            """
            SELECT COUNT(*) AS value
            FROM information_schema.columns
            WHERE table_name = :tableName
              AND column_name = :columnName
            """.trimIndent(),
        )
            .bind("tableName", tableName)
            .bind("columnName", columnName)
            .fetch()
            .one()
            .awaitSingle()["value"] as Number).toLong()) == 1L
}
