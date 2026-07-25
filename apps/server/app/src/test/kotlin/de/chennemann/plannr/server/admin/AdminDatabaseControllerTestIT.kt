package de.chennemann.plannr.server.admin

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOne
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class AdminDatabaseControllerTestIT {
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
    fun `wipe preserves fallback profile and removes other profiles`() = runBlocking {
        flyway().clean()
        flyway().migrate()
        execute(
            """
            INSERT INTO financial_profiles (
                name, description, is_default, is_archived, created_at, is_fallback
            )
            VALUES ('Alice', NULL, FALSE, FALSE, 1, FALSE)
            """,
        )

        val response = AdminDatabaseController(databaseClient).wipe()

        val profiles = databaseClient.sql(
            """
            SELECT id, name, is_fallback
            FROM financial_profiles
            ORDER BY id
            """.trimIndent(),
        )
            .map { row, _ ->
                PersistedProfile(
                    id = requireNotNull(row.get("id", java.lang.Long::class.java)).toLong(),
                    name = requireNotNull(row.get("name", String::class.java)),
                    isFallback = requireNotNull(row.get("is_fallback", java.lang.Boolean::class.java)).booleanValue(),
                )
            }
            .all()
            .collectList()
            .awaitSingle()

        assertTrue(response.truncatedTables > 0)
        assertEquals(1, profiles.size)
        assertEquals("Unassigned", profiles.single().name)
        assertTrue(profiles.single().isFallback)

        val nextProfileId = databaseClient.sql(
            """
            INSERT INTO financial_profiles (
                name, description, is_default, is_archived, created_at, is_fallback
            )
            VALUES ('Bob', NULL, FALSE, FALSE, 2, FALSE)
            RETURNING id
            """.trimIndent(),
        )
            .map { row, _ -> requireNotNull(row.get("id", java.lang.Long::class.java)).toLong() }
            .awaitOne()

        assertEquals(profiles.single().id + 1, nextProfileId)
        assertFalse(profiles.any { it.name == "Alice" })
    }

    private fun flyway(): Flyway = Flyway.configure()
        .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
        .locations("classpath:db/migration")
        .cleanDisabled(false)
        .load()

    private suspend fun execute(sql: String) {
        databaseClient.sql(sql.trimIndent()).fetch().rowsUpdated().awaitSingle()
    }

    private data class PersistedProfile(
        val id: Long,
        val name: String,
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
