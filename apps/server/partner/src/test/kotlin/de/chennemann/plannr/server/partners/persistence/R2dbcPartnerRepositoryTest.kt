package de.chennemann.plannr.server.partners.persistence

import de.chennemann.plannr.server.partners.domain.PartnerRepository
import de.chennemann.plannr.server.partners.persistence.toModel
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import de.chennemann.plannr.server.support.ApiIntegrationTest
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

        partnerRepository.save(partner.toModel())

        assertEquals(partner, partnerRepository.findById(PartnerFixtures.DEFAULT_ID))
        assertNull(partnerRepository.findById("par_missing"))
    }

    @Test
    fun `updates and finds partner by id`() = runBlocking {
        partnerRepository.save(PartnerFixtures.partner().toModel())
        val updated = PartnerFixtures.partner(name = "Updated", notes = null, isArchived = true)

        partnerRepository.update(updated.toModel())

        assertEquals(updated, partnerRepository.findById(PartnerFixtures.DEFAULT_ID))
    }

    @Test
    fun `finds all partners ordered by created at and id with filters`() = runBlocking {
        partnerRepository.save(PartnerFixtures.partner(id = "par_2", name = "Beta GmbH", createdAt = 2).toModel())
        partnerRepository.save(PartnerFixtures.partner(id = "par_1", name = "ACME Corp", createdAt = 1, isArchived = true).toModel())
        partnerRepository.save(PartnerFixtures.partner(id = "par_3", name = "Acme Services", createdAt = 3).toModel())

        val defaultList = partnerRepository.findAll()
        val queryList = partnerRepository.findAll(query = "acme")
        val archivedList = partnerRepository.findAll(archived = true)

        assertEquals(listOf("par_2", "par_3"), defaultList.map { it.id })
        assertEquals(listOf("par_3"), queryList.map { it.id })
        assertEquals(listOf("par_1"), archivedList.map { it.id })
    }
}
