package de.chennemann.plannr.server.pockets.usecases

import de.chennemann.plannr.server.common.error.NotFoundException
import de.chennemann.plannr.server.pockets.support.InMemoryPocketRepository
import de.chennemann.plannr.server.pockets.support.PocketFixtures
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ArchivePocketTest {
    @Test
    fun `archives pocket`() = runTest {
        val repository = InMemoryPocketRepository()
        repository.save(PocketFixtures.pocket())
        val archivePocket = ArchivePocketUseCase(repository)

        val result = archivePocket(PocketFixtures.DEFAULT_ID)

        assertEquals(true, result.isArchived)
        assertEquals(true, repository.findById(PocketFixtures.DEFAULT_ID)?.isArchived)
    }

    @Test
    fun `fails for unknown pocket`() = runTest {
        val archivePocket = ArchivePocketUseCase(InMemoryPocketRepository())

        assertFailsWith<NotFoundException> {
            archivePocket(PocketFixtures.DEFAULT_ID)
        }
    }
}
