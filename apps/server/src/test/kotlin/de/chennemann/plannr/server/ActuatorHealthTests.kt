package de.chennemann.plannr.server

import de.chennemann.plannr.server.support.ApiIntegrationTest
import org.junit.jupiter.api.Test

class ActuatorHealthTests : ApiIntegrationTest() {

    @Test
    fun `actuator health returns up`() {
        webTestClient.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
            .jsonPath("$.components.database.status").isEqualTo("UP")
    }
}
