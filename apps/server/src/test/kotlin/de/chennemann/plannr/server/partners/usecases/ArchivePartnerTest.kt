package de.chennemann.plannr.server.partners.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
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
        repository.save(PartnerFixtures.partner())
        val archivePartner = ArchivePartnerUseCase(repository)

        val result = archivePartner(PartnerFixtures.DEFAULT_ID)

        assertEquals(true, result.isArchived)
        assertEquals(true, repository.findById(PartnerFixtures.DEFAULT_ID)?.isArchived)
    }

    @Test
    fun `fails for unknown partner`() = runTest {
        val archivePartner = ArchivePartnerUseCase(InMemoryPartnerRepository())

        assertFailsWith<NotFoundException> {
            archivePartner(PartnerFixtures.DEFAULT_ID)
        }
    }
}
