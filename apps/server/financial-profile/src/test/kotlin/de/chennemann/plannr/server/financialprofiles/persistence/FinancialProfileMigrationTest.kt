package de.chennemann.plannr.server.financialprofiles.persistence

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
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
class FinancialProfileMigrationTest {
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

    @Test
    fun `backfills existing records and supports fallback reassignment`() = runBlocking {
        flyway().clean()
        flyway(target = "33").migrate()

        execute(
            """
            INSERT INTO accounts (id, name, institution, currency_code, weekend_handling, is_archived, created_at)
            VALUES (1, 'Main', 'Bank', 'EUR', 'NO_SHIFT', FALSE, 1)
            """,
        )
        execute(
            """
            INSERT INTO pockets (
                id, account_id, name, description, color, is_default, is_contract_pocket, is_archived, created_at
            )
            VALUES (1, 1, 'Phone', NULL, 123456, FALSE, TRUE, FALSE, 1)
            """,
        )
        execute(
            """
            INSERT INTO contracts (pocket_id, partner_id, signing_date, expiration_date, last_cancellation_date)
            VALUES (1, NULL, NULL, NULL, NULL)
            """,
        )
        execute(
            """
            INSERT INTO transaction_templates (
                id,
                source_pocket_id,
                destination_pocket_id,
                partner_id,
                title,
                description,
                amount,
                currency_code,
                transaction_type,
                first_occurrence_date,
                final_occurrence_date,
                recurrence_type,
                skip_count,
                days_of_week,
                weeks_of_month,
                days_of_month,
                months_of_year,
                previous_version_id,
                is_archived,
                created_at
            )
            VALUES (
                1, 1, NULL, NULL, 'Phone', NULL, 1000, 'EUR', 'EXPENSE',
                '2026-01-01', NULL, 'MONTHLY', 0, NULL, NULL, '1', NULL, NULL, FALSE, 1
            )
            """,
        )
        execute(
            """
            INSERT INTO transaction_materializations (
                id,
                transaction_template_id,
                transaction_date,
                source_pocket_id,
                destination_pocket_id,
                partner_id,
                title,
                description,
                amount,
                currency_code,
                transaction_type,
                created_at
            )
            VALUES (1, 1, '2026-01-01', 1, NULL, NULL, 'Phone', NULL, 1000, 'EUR', 'EXPENSE', 1)
            """,
        )

        flyway().migrate()

        val profile = databaseClient.sql(
            """
            SELECT id, name, kind, is_default, is_fallback
            FROM financial_profiles
            """,
        )
            .map { row, _ ->
                MigratedProfile(
                    id = requireNotNull(row.get("id", java.lang.Long::class.java)).toLong(),
                    name = requireNotNull(row.get("name", String::class.java)),
                    kind = requireNotNull(row.get("kind", String::class.java)),
                    isDefault = requireNotNull(row.get("is_default", java.lang.Boolean::class.java)).booleanValue(),
                    isFallback = requireNotNull(row.get("is_fallback", java.lang.Boolean::class.java)).booleanValue(),
                )
            }
            .awaitOne()

        assertEquals("Unassigned", profile.name)
        assertEquals("GROUP", profile.kind)
        assertEquals(true, profile.isDefault)
        assertEquals(true, profile.isFallback)

        listOf("contracts", "transaction_templates", "transaction_materializations").forEach { table ->
            val assignedId = databaseClient.sql("SELECT financial_profile_id FROM $table")
                .map { row, _ -> requireNotNull(row.get("financial_profile_id", java.lang.Long::class.java)).toLong() }
                .awaitOne()
            assertEquals(profile.id, assignedId)
        }

        execute(
            """
            INSERT INTO financial_profiles (
                id, name, description, kind, is_default, is_fallback, is_archived, created_at
            )
            VALUES (2, 'Alice', NULL, 'PERSON', FALSE, FALSE, FALSE, 2)
            """,
        )
        listOf("contracts", "transaction_templates", "transaction_materializations").forEach { table ->
            execute("UPDATE $table SET financial_profile_id = 2")
        }

        R2dbcFinancialProfileUsageRepository(databaseClient).reassignReferences(
            sourceProfileId = 2L,
            fallbackProfileId = profile.id,
            fallbackProfileName = profile.name,
            fallbackProfileKind = profile.kind,
        )

        listOf("contracts", "transaction_templates", "transaction_materializations").forEach { table ->
            val assignedId = databaseClient.sql("SELECT financial_profile_id FROM $table")
                .map { row, _ -> requireNotNull(row.get("financial_profile_id", java.lang.Long::class.java)).toLong() }
                .awaitOne()
            assertEquals(profile.id, assignedId)
        }
        execute("DELETE FROM financial_profiles WHERE id = 2")
    }

    private fun flyway(target: String? = null): Flyway {
        val configuration = Flyway.configure()
            .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
            .locations("classpath:db/migration")
            .cleanDisabled(false)
        target?.let(configuration::target)
        return configuration.load()
    }

    private suspend fun execute(sql: String) {
        databaseClient.sql(sql.trimIndent()).fetch().rowsUpdated().awaitSingle()
    }

    private data class MigratedProfile(
        val id: Long,
        val name: String,
        val kind: String,
        val isDefault: Boolean,
        val isFallback: Boolean,
    )

    companion object {
        @Container
        @JvmStatic
        private val postgres = PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("plannr")
            .withUsername("plannr")
            .withPassword("plannr")
    }
}
