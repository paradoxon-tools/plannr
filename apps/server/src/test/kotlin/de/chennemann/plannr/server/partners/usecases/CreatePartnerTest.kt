package de.chennemann.plannr.server.partners.usecases

import de.chennemann.plannr.server.partners.support.InMemoryPartnerRepository
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CreatePartnerTest {
    @Test
    fun `creates partner`() = runTest {
        val repository = InMemoryPartnerRepository()
        val createPartner = CreatePartnerUseCase(
            partnerRepository = repository,
            partnerIdGenerator = { PartnerFixtures.DEFAULT_ID },
            timeProvider = { PartnerFixtures.DEFAULT_CREATED_AT },
        )

        val created = createPartner(PartnerFixtures.createPartnerCommand())

        assertEquals(PartnerFixtures.DEFAULT_ID, created.id)
        assertEquals(created, repository.findById(PartnerFixtures.DEFAULT_ID))
    }
}
