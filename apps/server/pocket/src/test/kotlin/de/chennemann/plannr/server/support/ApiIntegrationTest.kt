package de.chennemann.plannr.server.support

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest(
    classes = [PocketTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
abstract class ApiIntegrationTest : PostgresContainerSupport(), DatabaseCleaner {
    @Autowired
    override lateinit var databaseClient: DatabaseClient
    @Autowired
    lateinit var environment: Environment

    @BeforeEach
    fun beforeEach() {
        Flyway.configure()
            .dataSource(
                environment.getRequiredProperty("spring.flyway.url"),
                environment.getRequiredProperty("spring.flyway.user"),
                environment.getRequiredProperty("spring.flyway.password"),
            )
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registerPostgresProperties(registry)
        }
    }
}
