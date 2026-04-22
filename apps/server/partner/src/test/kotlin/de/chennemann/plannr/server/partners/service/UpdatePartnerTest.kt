package de.chennemann.plannr.server.partners.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.support.InMemoryPartnerRepository
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdatePartnerTest {
    @Test
    fun `updates existing partner`() = runTest {
        val repository = InMemoryPartnerRepository()
        repository.save(PartnerFixtures.partner())
        val partnerService = PartnerServiceImpl(
            partnerRepository = repository,
            partnerIdGenerator = { PartnerFixtures.DEFAULT_ID },
            timeProvider = { PartnerFixtures.DEFAULT_CREATED_AT },
        )

        val updated = partnerService.update(
            PartnerFixtures.updatePartnerCommand(
                name = "Updated Partner",
                notes = null,
            ),
        )

        assertEquals("Updated Partner", updated.name)
        assertEquals(null, updated.notes)
        assertEquals(PartnerFixtures.DEFAULT_CREATED_AT, updated.createdAt)
    }

    @Test
    fun `fails when partner does not exist`() = runTest {
        val partnerService = PartnerServiceImpl(
            partnerRepository = InMemoryPartnerRepository(),
            partnerIdGenerator = { PartnerFixtures.DEFAULT_ID },
            timeProvider = { PartnerFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            partnerService.update(PartnerFixtures.updatePartnerCommand())
        }
    }
}
