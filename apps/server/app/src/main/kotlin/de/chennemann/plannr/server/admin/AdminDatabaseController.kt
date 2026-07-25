package de.chennemann.plannr.server.admin

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/database")
class AdminDatabaseController(
    private val databaseClient: DatabaseClient,
) {
    @DeleteMapping
    suspend fun wipe(): AdminDatabaseWipeResponse {
        val tables =
            databaseClient
                .sql(
                    """
                    SELECT COALESCE(string_agg(format('%I.%I', schemaname, tablename), ', '), '') AS tables
                    FROM pg_tables
                    WHERE schemaname = 'public'
                      AND tablename NOT IN ('flyway_schema_history', 'financial_profiles')
                    """.trimIndent(),
                ).fetch()
                .one()
                .map { row -> row.getValue("tables") as String }
                .awaitSingle()

        if (tables.isNotBlank()) {
            databaseClient
                .sql("TRUNCATE TABLE $tables RESTART IDENTITY CASCADE")
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        }

        databaseClient
            .sql("DELETE FROM financial_profiles WHERE is_fallback = FALSE")
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        databaseClient
            .sql(
                """
                SELECT setval(
                    pg_get_serial_sequence('financial_profiles', 'id'),
                    COALESCE((SELECT MAX(id) FROM financial_profiles), 1),
                    EXISTS (SELECT 1 FROM financial_profiles)
                ) AS sequence_value
                """.trimIndent(),
            ).fetch()
            .one()
            .awaitSingle()

        return AdminDatabaseWipeResponse(
            truncatedTables = tables.takeIf(String::isNotBlank)?.split(",")?.size ?: 0,
        )
    }
}

data class AdminDatabaseWipeResponse(
    val truncatedTables: Int,
)
