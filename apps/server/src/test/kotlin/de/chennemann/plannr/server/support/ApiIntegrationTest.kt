package de.chennemann.plannr.server.support

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class ApiIntegrationTest : PostgresContainerSupport() {
    @LocalServerPort
    private var port: Int = 0

    protected val webTestClient: WebTestClient by lazy {
        WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .build()
    }

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
