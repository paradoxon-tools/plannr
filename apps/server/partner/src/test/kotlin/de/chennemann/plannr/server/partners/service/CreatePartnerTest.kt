package de.chennemann.plannr.server.partners.service

import de.chennemann.plannr.server.common.events.NoOpApplicationEventBus
import de.chennemann.plannr.server.partners.support.InMemoryPartnerRepository
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CreatePartnerTest {
    @Test
    fun `creates partner`() = runTest {
        val repository = InMemoryPartnerRepository()
        val partnerService = PartnerServiceImpl(
            partnerRepository = repository,
            timeProvider = { PartnerFixtures.DEFAULT_CREATED_AT },
            applicationEventBus = NoOpApplicationEventBus,
        )

        val created = partnerService.create(PartnerFixtures.createPartnerCommand())

        assertEquals(created, repository.findById(created.id))
    }
}
