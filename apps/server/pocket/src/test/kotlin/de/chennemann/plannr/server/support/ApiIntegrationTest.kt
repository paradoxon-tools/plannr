package de.chennemann.plannr.server.support

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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

    @BeforeEach
    fun beforeEach() = Unit

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registerPostgresProperties(registry)
        }
    }
}
