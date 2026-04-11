package de.chennemann.plannr.server.support

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.postgresql.PostgreSQLContainer

abstract class PostgresContainerSupport {
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
            registry.add("plannr.database.host") { postgres.host }
            registry.add("plannr.database.port") { postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT) }
            registry.add("plannr.database.name") { postgres.databaseName }
            registry.add("plannr.database.username") { postgres.username }
            registry.add("plannr.database.password") { postgres.password }
        }
    }
}
