package de.chennemann.plannr.server.support

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

interface DatabaseCleaner {
    val databaseClient: DatabaseClient

    suspend fun cleanDatabase(vararg tables: String) {
        if (tables.isEmpty()) return
        databaseClient.sql("TRUNCATE TABLE ${tables.joinToString(", ")} RESTART IDENTITY CASCADE")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
