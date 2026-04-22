package de.chennemann.plannr.server.support

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

class DatabaseCleaner(private val databaseClient: DatabaseClient) {
    suspend fun clean(vararg tables: String) {
        if (tables.isEmpty()) return
        databaseClient.sql("TRUNCATE TABLE ${tables.joinToString(", ")} RESTART IDENTITY CASCADE")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
