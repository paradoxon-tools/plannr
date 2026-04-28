package de.chennemann.plannr.server.partners.service

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus
import de.chennemann.plannr.server.partners.persistence.toModel
import de.chennemann.plannr.server.partners.support.InMemoryPartnerRepository
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ArchivePartnerTest {
    @Test
    fun `archives partner`() = runTest {
        val repository = InMemoryPartnerRepository()
        repository.save(PartnerFixtures.partner().toModel())
        val partnerService = PartnerServiceImpl(
            partnerRepository = repository,
            timeProvider = { PartnerFixtures.DEFAULT_CREATED_AT },
            applicationEventBus = NoOpApplicationEventBus,
        )

        val result = partnerService.archive(PartnerFixtures.DEFAULT_ID)

        assertEquals(true, result.isArchived)
        assertEquals(true, repository.findById(PartnerFixtures.DEFAULT_ID)?.isArchived)
    }

    @Test
    fun `fails for unknown partner`() = runTest {
        val partnerService = PartnerServiceImpl(
            partnerRepository = InMemoryPartnerRepository(),
            timeProvider = { PartnerFixtures.DEFAULT_CREATED_AT },
            applicationEventBus = NoOpApplicationEventBus,
        )

        assertFailsWith<NotFoundException> {
            partnerService.archive(PartnerFixtures.DEFAULT_ID)
        }
    }
}
