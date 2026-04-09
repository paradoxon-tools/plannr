package de.chennemann.plannr.server.health

import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.ReactiveHealthIndicator
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component("database")
class DatabaseHealthIndicator(
    private val databaseClient: DatabaseClient
) : ReactiveHealthIndicator {
    override fun health(): Mono<Health> =
        databaseClient.sql("SELECT 1")
            .map { _, _ -> 1 }
            .one()
            .map {
                Health.up()
                    .withDetail("database", "reachable")
                    .build()
            }
            .onErrorResume { exception ->
                Mono.just(
                    Health.down(exception)
                        .withDetail("database", "unreachable")
                        .build()
                )
            }
}
