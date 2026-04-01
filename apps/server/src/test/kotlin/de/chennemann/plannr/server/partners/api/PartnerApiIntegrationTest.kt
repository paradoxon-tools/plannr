package de.chennemann.plannr.server.partners.api

import de.chennemann.plannr.server.partners.support.PartnerFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import de.chennemann.plannr.server.support.expectApiError
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PartnerApiIntegrationTest : ApiIntegrationTest() {
    @BeforeEach
    fun setUp() {
        cleanDatabase("partners")
    }

    @Test
    fun `creates and fetches partner over http`() {
        webTestClient.post()
            .uri("/partners")
            .bodyValue(PartnerFixtures.createPartnerRequest())
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.name").isEqualTo(PartnerFixtures.DEFAULT_NAME)
            .jsonPath("$.notes").isEqualTo(PartnerFixtures.DEFAULT_NOTES)
            .jsonPath("$.isArchived").isEqualTo(false)
            .jsonPath("$.id").exists()
            .jsonPath("$.createdAt").exists()
    }

    @Test
    fun `gets partner by id`() {
        val partnerId = createPartnerOverHttp()

        webTestClient.get()
            .uri("/partners/{id}", partnerId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(partnerId)
            .jsonPath("$.name").isEqualTo(PartnerFixtures.DEFAULT_NAME)
    }

    @Test
    fun `lists partners and excludes archived by default and supports search`() {
        createPartnerOverHttp(name = "ACME Corp")
        val archivedId = createPartnerOverHttp(name = "Beta GmbH")
        createPartnerOverHttp(name = "Acme Services")
        archivePartnerOverHttp(archivedId)

        webTestClient.get()
            .uri("/partners")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].name").isEqualTo("ACME Corp")
            .jsonPath("$[1].name").isEqualTo("Acme Services")

        webTestClient.get()
            .uri { builder -> builder.path("/partners").queryParam("query", "acme").build() }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].name").isEqualTo("ACME Corp")
            .jsonPath("$[1].name").isEqualTo("Acme Services")

        webTestClient.get()
            .uri { builder -> builder.path("/partners").queryParam("archived", true).build() }
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(archivedId)
            .jsonPath("$[0].isArchived").isEqualTo(true)
    }

    @Test
    fun `updates existing partner`() {
        val partnerId = createPartnerOverHttp()

        webTestClient.put()
            .uri("/partners/{id}", partnerId)
            .bodyValue(PartnerFixtures.updatePartnerRequest(name = "Updated Partner", notes = null))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(partnerId)
            .jsonPath("$.name").isEqualTo("Updated Partner")
            .jsonPath("$.notes").isEmpty
    }

    @Test
    fun `archives and unarchives partner`() {
        val partnerId = createPartnerOverHttp()

        webTestClient.post()
            .uri("/partners/{id}/archive", partnerId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(true)

        webTestClient.post()
            .uri("/partners/{id}/unarchive", partnerId)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.isArchived").isEqualTo(false)
    }

    @Test
    fun `returns validation error for invalid create request`() {
        webTestClient.post()
            .uri("/partners")
            .bodyValue(PartnerFixtures.createPartnerRequest(name = "   "))
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .expectApiError(
                code = "validation_error",
                message = "Partner name must not be blank",
            )
    }

    @Test
    fun `returns not found for unknown partner`() {
        webTestClient.get()
            .uri("/partners/par_missing")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .expectApiError(
                code = "not_found",
                message = "Partner not found",
                details = mapOf("id" to "par_missing"),
            )
    }

    private fun createPartnerOverHttp(name: String = PartnerFixtures.DEFAULT_NAME): String {
        val response = webTestClient.post()
            .uri("/partners")
            .bodyValue(PartnerFixtures.createPartnerRequest(name = name))
            .exchange()
            .expectStatus().isCreated
            .expectBody(PartnerResponse::class.java)
            .returnResult()
            .responseBody!!

        return response.id
    }

    private fun archivePartnerOverHttp(id: String) {
        webTestClient.post()
            .uri("/partners/{id}/archive", id)
            .exchange()
            .expectStatus().isOk
    }
}
