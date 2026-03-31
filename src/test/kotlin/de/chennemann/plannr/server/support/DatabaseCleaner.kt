package de.chennemann.plannr.server.support

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.springframework.r2dbc.core.DatabaseClient

class DatabaseCleaner(
    private val databaseClient: DatabaseClient,
) {
    fun deleteAllFrom(vararg tables: String) {
        runBlocking {
            databaseClient.sql("SET REFERENTIAL_INTEGRITY FALSE").fetch().rowsUpdated().awaitSingle()
            try {
                tables.forEach { table ->
                    databaseClient.sql("DELETE FROM $table").fetch().rowsUpdated().awaitSingle()
                }
            } finally {
                databaseClient.sql("SET REFERENTIAL_INTEGRITY TRUE").fetch().rowsUpdated().awaitSingle()
            }
        }
    }
}
