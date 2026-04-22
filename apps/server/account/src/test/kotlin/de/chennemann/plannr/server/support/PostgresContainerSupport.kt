package de.chennemann.plannr.server.support

import org.junit.jupiter.api.Tag
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Tag("integration")
@Testcontainers
abstract class PostgresContainerSupport {
    companion object {
        @Container
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")

        class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
            override fun initialize(applicationContext: ConfigurableApplicationContext) {
                postgres.start()
                TestPropertyValues.of(
                    "spring.r2dbc.url=r2dbc:postgresql://${postgres.host}:${postgres.getMappedPort(5432)}/${postgres.databaseName}",
                    "spring.r2dbc.username=${postgres.username}",
                    "spring.r2dbc.password=${postgres.password}",
                    "spring.flyway.url=${postgres.jdbcUrl}",
                    "spring.flyway.user=${postgres.username}",
                    "spring.flyway.password=${postgres.password}",
                ).applyTo(applicationContext.environment)
            }
        }
    }
}
