package de.chennemann.plannr.server.partners.persistence

import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.partners.persistence.toDomain
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNull

class R2dbcPartnerRepositoryTest : ApiIntegrationTest() {
    @Autowired
    lateinit var partnerRepository: PartnerRepository

    @BeforeEach
    fun setUp() {
        cleanDatabase("partners")
    }

    @Test
    fun `saves and finds partner by id`() = runBlocking {
        val partner = PartnerFixtures.partner()

        partnerRepository.insert(
            id = partner.id,
            name = partner.name,
            notes = partner.notes,
            isArchived = partner.isArchived,
            createdAt = partner.createdAt,
        )

        assertEquals(partner, partnerRepository.findById(PartnerFixtures.DEFAULT_ID)?.toDomain())
        assertNull(partnerRepository.findById("par_missing"))
    }

    @Test
    fun `updates and finds partner by id`() = runBlocking {
        val original = PartnerFixtures.partner()
        partnerRepository.insert(
            id = original.id,
            name = original.name,
            notes = original.notes,
            isArchived = original.isArchived,
            createdAt = original.createdAt,
        )
        val updated = PartnerFixtures.partner(name = "Updated", notes = null, isArchived = true)

        partnerRepository.update(
            id = updated.id,
            name = updated.name,
            notes = updated.notes,
            isArchived = updated.isArchived,
        )

        assertEquals(updated, partnerRepository.findById(PartnerFixtures.DEFAULT_ID)?.toDomain())
    }

    @Test
    fun `finds all partners ordered by created at and id with filters`() = runBlocking {
        partnerRepository.insert(id = "par_2", name = "Beta GmbH", notes = null, isArchived = false, createdAt = 2)
        partnerRepository.insert(id = "par_1", name = "ACME Corp", notes = null, isArchived = true, createdAt = 1)
        partnerRepository.insert(id = "par_3", name = "Acme Services", notes = null, isArchived = false, createdAt = 3)

        val defaultList = partnerRepository.findAllByQueryAndArchived(query = null, archived = false).toList()
        val queryList = partnerRepository.findAllByQueryAndArchived(query = "acme", archived = false).toList()
        val archivedList = partnerRepository.findAllByQueryAndArchived(query = null, archived = true).toList()

        assertEquals(listOf("par_2", "par_3"), defaultList.map { it.id })
        assertEquals(listOf("par_3"), queryList.map { it.id })
        assertEquals(listOf("par_1"), archivedList.map { it.id })
    }
}
