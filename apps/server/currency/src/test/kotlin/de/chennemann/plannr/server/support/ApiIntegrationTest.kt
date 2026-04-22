package de.chennemann.plannr.server.support

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient

@Tag("integration")
@SpringBootTest(classes = [CurrencyTestApplication::class])
abstract class ApiIntegrationTest : PostgresContainerSupport() {
    @Autowired
    private lateinit var databaseClient: DatabaseClient

    @BeforeEach
    fun ensureSchema() {
        Flyway.configure()
            .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }

    protected fun cleanDatabase(vararg tables: String) {
        DatabaseCleaner(databaseClient).deleteAllFrom(*tables)
    }
}
