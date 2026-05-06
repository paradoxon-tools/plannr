package de.chennemann.plannr.server.partners.persistence

import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.partners.persistence.toDomain
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.r2dbc.core.DatabaseClient
import kotlin.test.assertEquals
import kotlin.test.assertNull

class R2dbcPartnerRepositoryTest : ApiIntegrationTest() {
    @Autowired
    lateinit var partnerRepository: PartnerRepository
    @Autowired
    lateinit var testDatabaseClient: DatabaseClient

    @BeforeEach
    fun setUp() {
        cleanDatabase("partners")
    }

    @Test
    fun `saves and finds partner by id`() = runBlocking {
        val partner = PartnerFixtures.partner()

        val saved = partnerRepository.save(partner.toModel().copy(id = null))

        assertEquals(partner.copy(id = saved.toDomain().id), partnerRepository.findById(saved.toDomain().id)?.toDomain())
        assertNull(partnerRepository.findById(999L))
    }

    @Test
    fun `updates and finds partner by id`() = runBlocking {
        val original = PartnerFixtures.partner()
        insertPartner(original.toModel())
        val updated = PartnerFixtures.partner(name = "Updated", notes = null, isArchived = true)

        partnerRepository.save(updated.toModel())

        assertEquals(updated, partnerRepository.findById(PartnerFixtures.DEFAULT_ID)?.toDomain())
    }

    @Test
    fun `finds all partners ordered by created at and id with filters`() = runBlocking {
        insertPartner(PartnerModel(id = 2L, name = "Beta GmbH", notes = null, isArchived = false, createdAt = 2))
        insertPartner(PartnerModel(id = 1L, name = "ACME Corp", notes = null, isArchived = true, createdAt = 1))
        insertPartner(PartnerModel(id = 3L, name = "Acme Services", notes = null, isArchived = false, createdAt = 3))

        val defaultList = partnerRepository.findAllByQueryAndArchived(query = null, archived = false).toList()
        val queryList = partnerRepository.findAllByQueryAndArchived(query = "acme", archived = false).toList()
        val archivedList = partnerRepository.findAllByQueryAndArchived(query = null, archived = true).toList()

        assertEquals(listOf(2L, 3L), defaultList.map { it.id })
        assertEquals(listOf(3L), queryList.map { it.id })
        assertEquals(listOf(1L), archivedList.map { it.id })
    }

    private suspend fun insertPartner(partner: PartnerModel) {
        val spec = testDatabaseClient.sql(
            """
            INSERT INTO partners (id, name, notes, is_archived, created_at)
            VALUES (:id, :name, :notes, :isArchived, :createdAt)
            """.trimIndent(),
        )
            .bind("id", requireNotNull(partner.id))
            .bind("name", partner.name)
            .bind("isArchived", partner.isArchived)
            .bind("createdAt", partner.createdAt)
        val boundSpec = partner.notes?.let { spec.bind("notes", it) } ?: spec.bindNull("notes", String::class.java)
        boundSpec
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
