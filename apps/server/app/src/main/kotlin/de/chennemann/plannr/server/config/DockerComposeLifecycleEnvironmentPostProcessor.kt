package de.chennemann.plannr.server.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource

class DockerComposeLifecycleEnvironmentPostProcessor : EnvironmentPostProcessor {
    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        if (!usesExternalDatabase(environment)) {
            return
        }

        environment.propertySources.addFirst(
            MapPropertySource(
                PROPERTY_SOURCE_NAME,
                mapOf(LIFECYCLE_PROPERTY to "none")
            )
        )
    }

    private fun usesExternalDatabase(environment: ConfigurableEnvironment): Boolean {
        if (environment.getProperty("spring.r2dbc.url") != DEFAULT_R2DBC_URL) {
            return true
        }

        if (environment.getProperty("spring.flyway.url") != DEFAULT_JDBC_URL) {
            return true
        }

        return DEFAULT_DATABASE_PROPERTIES.any { (property, defaultValue) ->
            environment.getProperty(property) != defaultValue
        }
    }

    companion object {
        private const val PROPERTY_SOURCE_NAME = "plannrDockerComposeLifecycle"
        private const val LIFECYCLE_PROPERTY = "plannr.database.docker-compose.lifecycle-management"
        private const val DEFAULT_HOST = "localhost"
        private const val DEFAULT_PORT = "15432"
        private const val DEFAULT_NAME = "plannr"
        private const val DEFAULT_USERNAME = "plannr"
        private const val DEFAULT_PASSWORD = "plannr"
        private const val DEFAULT_R2DBC_URL = "r2dbc:postgresql://$DEFAULT_HOST:$DEFAULT_PORT/$DEFAULT_NAME"
        private const val DEFAULT_JDBC_URL = "jdbc:postgresql://$DEFAULT_HOST:$DEFAULT_PORT/$DEFAULT_NAME"
        private val DEFAULT_DATABASE_PROPERTIES = mapOf(
            "plannr.database.host" to DEFAULT_HOST,
            "plannr.database.port" to DEFAULT_PORT,
            "plannr.database.name" to DEFAULT_NAME,
            "plannr.database.username" to DEFAULT_USERNAME,
            "plannr.database.password" to DEFAULT_PASSWORD,
        )
    }
}
