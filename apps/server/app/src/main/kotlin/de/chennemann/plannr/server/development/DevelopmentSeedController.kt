package de.chennemann.plannr.server.development

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile("local")
@ConditionalOnProperty(prefix = "plannr.dev-seed", name = ["enabled"], havingValue = "true")
@RequestMapping("/internal/dev/seed")
class DevelopmentSeedController(
    private val seeder: DevelopmentDataSeeder,
) {
    @PostMapping
    suspend fun seedDefaultScenario(): DevelopmentSeedResult =
        seeder.seedDefaultScenario()
}
