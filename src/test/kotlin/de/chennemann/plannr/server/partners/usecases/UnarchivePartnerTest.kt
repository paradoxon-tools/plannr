package de.chennemann.plannr.server.partners.usecases

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
        val unarchivePartner = UnarchivePartnerUseCase(repository)

        val result = unarchivePartner(PartnerFixtures.DEFAULT_ID)

        assertEquals(false, result.isArchived)
        assertEquals(false, repository.findById(PartnerFixtures.DEFAULT_ID)?.isArchived)
    }

    @Test
    fun `fails for unknown partner`() = runTest {
        val unarchivePartner = UnarchivePartnerUseCase(InMemoryPartnerRepository())

        assertFailsWith<NotFoundException> {
            unarchivePartner(PartnerFixtures.DEFAULT_ID)
        }
    }
}
