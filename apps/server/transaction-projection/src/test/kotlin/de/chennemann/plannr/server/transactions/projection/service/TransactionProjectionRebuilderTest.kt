package de.chennemann.plannr.server.transactions.projection.service

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOne
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals

@Tag("integration")
@Testcontainers
class TransactionProjectionRebuilderTest {
    private val databaseClient: DatabaseClient
        get() = DatabaseClient.create(
            ConnectionFactories.get(
                ConnectionFactoryOptions.builder()
                    .option(ConnectionFactoryOptions.DRIVER, "postgresql")
                    .option(ConnectionFactoryOptions.HOST, postgres.host)
                    .option(ConnectionFactoryOptions.PORT, postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT))
                    .option(ConnectionFactoryOptions.USER, postgres.username)
                    .option(ConnectionFactoryOptions.PASSWORD, postgres.password)
                    .option(ConnectionFactoryOptions.DATABASE, postgres.databaseName)
                    .build(),
            ),
        )

    @BeforeEach
    fun setUp() {
        Flyway.configure()
            .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
            .locations("classpath:db/migration")
            .load()
            .migrate()

        runBlocking {
            execute(
                """
                TRUNCATE TABLE
                    transaction_projection_events,
                    contract_transaction_feed,
                    pocket_transaction_feed,
                    account_transaction_feed,
                    transaction_materializations,
                    transaction_template_versions,
                    transaction_templates,
                    contracts,
                    pockets,
                    accounts,
                    partners
                RESTART IDENTITY CASCADE
                """,
            )
        }
    }

    @Test
    fun `rebuilds all feeds with financial profile snapshot`() = runBlocking {
        val profileId = databaseClient.sql("SELECT id FROM financial_profiles WHERE is_default = TRUE")
            .map { row, _ -> requireNotNull(row.get("id", java.lang.Long::class.java)).toLong() }
            .awaitOne()

        execute(
            """
            INSERT INTO accounts (id, name, institution, currency_code, weekend_handling, is_archived, created_at)
            VALUES (1, 'Main', 'Bank', 'EUR', 'NO_SHIFT', FALSE, 1)
            """,
        )
        execute(
            """
            INSERT INTO contracts (
                id, financial_profile_id, partner_id, name, description, color, type,
                signing_date, expiration_date, last_cancellation_date, is_archived, created_at
            )
            VALUES (1, $profileId, NULL, 'Mobile plan', NULL, 123456, 'ACCUMULATING',
                    NULL, NULL, NULL, FALSE, 1)
            """,
        )
        execute(
            """
            INSERT INTO pockets (
                id, account_id, contract_id, name, description, color, is_default, is_archived, created_at
            )
            VALUES (1, 1, 1, NULL, NULL, NULL, FALSE, FALSE, 1)
            """,
        )
        execute(
            """
            INSERT INTO transaction_templates (
                id, contract_id, source_pocket_id, destination_pocket_id, financial_profile_id,
                partner_id, title, description, currency_code, transaction_type, is_archived, created_at
            )
            VALUES (1, 1, 1, NULL, $profileId, NULL, 'Phone', NULL, 'EUR', 'EXPENSE', FALSE, 1)
            """,
        )
        execute(
            """
            INSERT INTO transaction_template_versions (
                id, transaction_template_id, amount,
                first_occurrence_date,
                final_occurrence_date,
                recurrence_type,
                skip_count,
                days_of_week,
                weeks_of_month,
                days_of_month,
                months_of_year,
                valid_from,
                valid_until,
                created_at
            )
            VALUES (
                1, 1, 1000, '2026-01-01', NULL, 'MONTHLY', 0,
                NULL, NULL, '1', NULL, '2026-01-01', NULL, 1
            )
            """,
        )
        execute(
            """
            INSERT INTO transaction_materializations (
                id,
                transaction_template_id,
                transaction_template_version_id,
                contract_id,
                transaction_date,
                source_pocket_id,
                destination_pocket_id,
                financial_profile_id,
                partner_id,
                title,
                description,
                amount,
                currency_code,
                transaction_type,
                created_at
            )
            VALUES (1, 1, 1, 1, '2026-01-01', 1, NULL, $profileId, NULL, 'Phone', NULL, 1000, 'EUR', 'EXPENSE', 1)
            """,
        )

        TransactionProjectionRebuilder(databaseClient).rebuildAll()

        listOf("account_transaction_feed", "pocket_transaction_feed", "contract_transaction_feed").forEach { table ->
            val snapshot = databaseClient.sql(
                """
                SELECT financial_profile_id, financial_profile_name
                FROM $table
                """,
            )
                .map { row, _ ->
                    Pair(
                        requireNotNull(row.get("financial_profile_id", java.lang.Long::class.java)).toLong(),
                        requireNotNull(row.get("financial_profile_name", String::class.java)),
                    )
                }
                .awaitOne()

            assertEquals(Pair(profileId, "Unassigned"), snapshot)
        }

        val feedItem = TransactionFeedServiceImpl(databaseClient)
            .getForAccount(id = 1L, cursor = null, limit = 50)
            .transactions
            .single()

        assertEquals(profileId, feedItem.financialProfile.id)
        assertEquals("Unassigned", feedItem.financialProfile.name)
    }

    private suspend fun execute(sql: String) {
        databaseClient.sql(sql.trimIndent()).fetch().rowsUpdated().awaitSingle()
    }

    companion object {
        @Container
        @JvmStatic
        private val postgres = PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("plannr")
            .withUsername("plannr")
            .withPassword("plannr")
    }
}
