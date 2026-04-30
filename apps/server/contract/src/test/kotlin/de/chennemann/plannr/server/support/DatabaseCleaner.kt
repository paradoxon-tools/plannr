package de.chennemann.plannr.server.support

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.springframework.r2dbc.core.DatabaseClient

class DatabaseCleaner(
    private val databaseClient: DatabaseClient,
) {
    fun deleteAllFrom(vararg tables: String) {
        if (tables.isEmpty()) return

        runBlocking {
            databaseClient.sql("TRUNCATE TABLE ${tables.joinToString(", ")} CASCADE")
                .fetch()
                .rowsUpdated()
                .awaitSingle()
        }
    }
}
