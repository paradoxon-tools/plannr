package de.chennemann.plannr.server.support

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.postgresql.PostgreSQLContainer

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class ApiIntegrationTest {
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

    companion object {
        @JvmField
        protected val postgres = PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("plannr_test")
            .withUsername("plannr")
            .withPassword("plannr")
            .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            val jdbcUrl = postgres.jdbcUrl
            val r2dbcUrl = "r2dbc:postgresql://${postgres.host}:${postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)}/${postgres.databaseName}"

            registry.add("spring.datasource.url") { jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.r2dbc.url") { r2dbcUrl }
            registry.add("spring.r2dbc.username") { postgres.username }
            registry.add("spring.r2dbc.password") { postgres.password }
        }
    }
}
