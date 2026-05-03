package de.chennemann.plannr.server.support

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

class DatabaseCleaner(
    private val databaseClient: DatabaseClient,
) {
    suspend fun deleteAllFrom(vararg tables: String) {
        tables.forEach { table ->
            databaseClient.sql("""TRUNCATE TABLE "$table" RESTART IDENTITY CASCADE""")
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        }
    }
}
