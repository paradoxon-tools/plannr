package de.chennemann.plannr.server.partners.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.partners.support.InMemoryPartnerRepository
import de.chennemann.plannr.server.partners.support.PartnerFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetPartnerTest {
    @Test
    fun `returns partner by id`() = runTest {
        val repository = InMemoryPartnerRepository()
        val partner = PartnerFixtures.partner()
        repository.save(partner)
        val getPartner = GetPartnerUseCase(repository)

        val result = getPartner(PartnerFixtures.DEFAULT_ID)

        assertEquals(partner, result)
    }

    @Test
    fun `fails for unknown partner`() = runTest {
        val getPartner = GetPartnerUseCase(InMemoryPartnerRepository())

        assertFailsWith<NotFoundException> {
            getPartner(PartnerFixtures.DEFAULT_ID)
        }
    }
}
