package de.chennemann.plannr.server.partners.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.support.InMemoryPartnerRepository
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnarchivePartnerTest {
    @Test
    fun `unarchives partner`() = runTest {
        val repository = InMemoryPartnerRepository()
        repository.save(PartnerFixtures.partner(isArchived = true))
        val partnerService = PartnerServiceImpl(
            partnerRepository = repository,
            partnerIdGenerator = { PartnerFixtures.DEFAULT_ID },
            timeProvider = { PartnerFixtures.DEFAULT_CREATED_AT },
        )

        val result = partnerService.unarchive(PartnerFixtures.DEFAULT_ID)

        assertEquals(false, result.isArchived)
        assertEquals(false, repository.findById(PartnerFixtures.DEFAULT_ID)?.isArchived)
    }

    @Test
    fun `fails for unknown partner`() = runTest {
        val partnerService = PartnerServiceImpl(
            partnerRepository = InMemoryPartnerRepository(),
            partnerIdGenerator = { PartnerFixtures.DEFAULT_ID },
            timeProvider = { PartnerFixtures.DEFAULT_CREATED_AT },
        )

        assertFailsWith<NotFoundException> {
            partnerService.unarchive(PartnerFixtures.DEFAULT_ID)
        }
    }
}
