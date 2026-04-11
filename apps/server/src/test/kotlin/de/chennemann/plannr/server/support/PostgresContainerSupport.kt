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
