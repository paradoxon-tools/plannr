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
        val tables = databaseClient.sql(
            """
            SELECT COALESCE(string_agg(format('%I.%I', schemaname, tablename), ', '), '') AS tables
            FROM pg_tables
            WHERE schemaname = 'public'
              AND tablename <> 'flyway_schema_history'
            """.trimIndent(),
        )
            .fetch()
            .one()
            .map { row -> row.getValue("tables") as String }
            .awaitSingle()

        if (tables.isBlank()) {
            return AdminDatabaseWipeResponse(truncatedTables = 0)
        }

        databaseClient.sql("TRUNCATE TABLE $tables RESTART IDENTITY CASCADE")
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        return AdminDatabaseWipeResponse(
            truncatedTables = tables.split(",").size,
        )
    }
}

data class AdminDatabaseWipeResponse(
    val truncatedTables: Int,
)
