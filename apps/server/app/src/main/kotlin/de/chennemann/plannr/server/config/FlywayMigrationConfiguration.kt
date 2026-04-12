package de.chennemann.plannr.server.config

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlywayMigrationConfiguration(
    @Value("\${spring.flyway.url}") private val url: String,
    @Value("\${spring.flyway.user}") private val user: String,
    @Value("\${spring.flyway.password}") private val password: String,
) {
    @Bean(initMethod = "migrate")
    fun flyway(): Flyway = Flyway.configure()
        .dataSource(url, user, password)
        .locations("classpath:db/migration")
        .load()
}
