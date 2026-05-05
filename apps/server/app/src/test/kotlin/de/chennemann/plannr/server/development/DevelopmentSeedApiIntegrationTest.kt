package de.chennemann.plannr.server.development

import de.chennemann.plannr.server.support.ApiIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test", "local")
class DevelopmentSeedApiIntegrationTest : ApiIntegrationTest() {
    @BeforeEach
    fun setUp() {
        cleanDatabase(
            "recurring_transactions",
            "contracts",
            "partners",
            "pockets",
            "accounts",
        )
    }

    @Test
    fun `seed endpoint is exposed for local profile`() {
        webTestClient.post()
            .uri("/internal/dev/seed")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.scenario").isEqualTo("default-development")
            .jsonPath("$.accounts.length()").isEqualTo(1)
            .jsonPath("$.pockets.length()").isEqualTo(4)
            .jsonPath("$.partners.length()").isEqualTo(3)
            .jsonPath("$.contracts.length()").isEqualTo(3)
            .jsonPath("$.recurringTransactions.length()").isEqualTo(3)
    }
}
