package de.chennemann.plannr.server.migration

import de.chennemann.plannr.server.projection.TransactionQueryProjectionService
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import kotlin.test.assertEquals

class MigrationBackfillIntegrationTest : ApiIntegrationTest() {
    @Autowired lateinit var databaseClient: DatabaseClient
    @Autowired lateinit var projectionService: TransactionQueryProjectionService

    @Test
    fun `legacy enum and selector values are normalized during migration and projections can be rebuilt`() = runBlocking {
        flyway().clean()
        flyway(target = "8").migrate()
        seedLegacyData()

        flyway().migrate()
        projectionService.rebuildAll()

        assertEquals("MOVE_AFTER", singleString("SELECT weekend_handling AS value FROM accounts WHERE id = 'acc_123'"))
        assertEquals("EXPENSE", singleString("SELECT type AS value FROM transactions WHERE id = 'txn_past'"))
        assertEquals("CLEARED", singleString("SELECT status AS value FROM transactions WHERE id = 'txn_past'"))
        assertEquals("poc_123", singleString("SELECT pocket_id AS value FROM transactions WHERE id = 'txn_past'"))
        assertEquals("YEARLY", singleString("SELECT recurrence_type AS value FROM recurring_transactions WHERE id = 'rtx_123'"))
        assertEquals("MONDAY,WEDNESDAY", singleString("SELECT days_of_week AS value FROM recurring_transactions WHERE id = 'rtx_123'"))
        assertEquals("-1,2", singleString("SELECT weeks_of_month AS value FROM recurring_transactions WHERE id = 'rtx_123'"))
        assertEquals("-1,10", singleString("SELECT days_of_month AS value FROM recurring_transactions WHERE id = 'rtx_123'"))
        assertEquals("1,6", singleString("SELECT months_of_year AS value FROM recurring_transactions WHERE id = 'rtx_123'"))

        assertEquals(-100L, singleLong("SELECT current_balance AS value FROM account_query WHERE account_id = 'acc_123'"))
        assertEquals(-100L, singleLong("SELECT current_balance AS value FROM pocket_query WHERE pocket_id = 'poc_123'"))
        assertEquals(1L, singleLong("SELECT COUNT(*) AS value FROM account_transaction_feed WHERE account_id = 'acc_123'"))
        assertEquals(1L, singleLong("SELECT COUNT(*) AS value FROM account_future_transaction_feed WHERE account_id = 'acc_123'"))
        assertEquals(1L, singleLong("SELECT COUNT(*) AS value FROM pocket_future_transaction_feed WHERE pocket_id = 'poc_123'"))
    }

    private fun flyway(target: String? = null): Flyway {
        val configuration = Flyway.configure()
            .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
            .locations("classpath:db/migration")
            .cleanDisabled(false)
        if (target != null) configuration.target(target)
        return configuration.load()
    }

    private fun seedLegacyData() = runBlocking {
        databaseClient.sql("INSERT INTO currencies (code, name, symbol, decimal_places, symbol_position) VALUES ('EUR', 'Euro', '€', 2, 'before')").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("INSERT INTO accounts (id, name, institution, currency_code, weekend_handling, is_archived, created_at) VALUES ('acc_123', 'Main', 'Bank', 'EUR', 'next_business_day', FALSE, 1)").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("INSERT INTO pockets (id, account_id, name, description, color, is_default, is_archived, created_at) VALUES ('poc_123', 'acc_123', 'Bills', NULL, 123, TRUE, FALSE, 1)").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("INSERT INTO account_query (account_id, name, institution, currency_code, weekend_handling, is_archived, created_at, current_balance) VALUES ('acc_123', 'Main', 'Bank', 'EUR', 'next_business_day', FALSE, 1, 0)").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("INSERT INTO pocket_query (pocket_id, account_id, name, description, color, is_default, is_archived, created_at, current_balance) VALUES ('poc_123', 'acc_123', 'Bills', NULL, 123, TRUE, FALSE, 1, 0)").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("INSERT INTO transactions (id, account_id, type, status, transaction_date, amount, currency_code, exchange_rate, destination_amount, description, partner_id, source_pocket_id, destination_pocket_id, parent_transaction_id, recurring_transaction_id, modified_by_id, is_archived, created_at) VALUES ('txn_past', 'acc_123', 'expense', 'booked', '2020-04-10', 100, 'EUR', NULL, NULL, 'Past', NULL, 'poc_123', NULL, NULL, NULL, NULL, FALSE, 1)").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("INSERT INTO transactions (id, account_id, type, status, transaction_date, amount, currency_code, exchange_rate, destination_amount, description, partner_id, source_pocket_id, destination_pocket_id, parent_transaction_id, recurring_transaction_id, modified_by_id, is_archived, created_at) VALUES ('txn_future', 'acc_123', 'expense', 'pending', '2099-04-20', 200, 'EUR', NULL, NULL, 'Future', NULL, 'poc_123', NULL, NULL, NULL, NULL, FALSE, 2)").fetch().rowsUpdated().awaitSingle()
        databaseClient.sql("INSERT INTO recurring_transactions (id, contract_id, account_id, source_pocket_id, destination_pocket_id, partner_id, title, description, amount, currency_code, transaction_type, first_occurrence_date, final_occurrence_date, recurrence_type, skip_count, days_of_week, weeks_of_month, days_of_month, months_of_year, last_materialized_date, previous_version_id, is_archived, created_at) VALUES ('rtx_123', NULL, 'acc_123', 'poc_123', NULL, NULL, 'Legacy', 'Legacy', 100, 'EUR', 'expense', '2024-01-01', NULL, 'yearly', 0, 'WEDNESDAY,MONDAY,MONDAY', '2,-1,2', '10,-1,10', '6,1,6', NULL, NULL, FALSE, 1)").fetch().rowsUpdated().awaitSingle()
    }

    private suspend fun singleString(sql: String): String = databaseClient.sql(sql).fetch().one().awaitSingle().getValue("value") as String
    private suspend fun singleLong(sql: String): Long = (databaseClient.sql(sql).fetch().one().awaitSingle().getValue("value") as Number).toLong()
}
